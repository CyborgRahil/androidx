/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.video.internal.encoder

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.CamcorderProfile
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.CameraXConfig
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.impl.CamcorderProfileProxy
import androidx.camera.core.impl.CameraInfoInternal
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.core.internal.CameraUseCaseAdapter
import androidx.camera.testing.CameraUtil
import androidx.camera.testing.SurfaceTextureProvider
import androidx.camera.testing.SurfaceTextureProvider.SurfaceTextureCallback
import androidx.camera.video.internal.compat.quirk.DeviceQuirks
import androidx.camera.video.internal.compat.quirk.DeactivateEncoderSurfaceBeforeStopEncoderQuirk
import androidx.concurrent.futures.ResolvableFuture
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.AssumptionViolatedException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.invocation.InvocationOnMock
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

private const val TAG = "VideoEncoderTest"

@LargeTest
@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class VideoEncoderTest {

    @get: Rule
    var cameraRule: TestRule = CameraUtil.grantCameraPermissionAndPreTest()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var currentSurface: Surface? = null

    private lateinit var camera: CameraUseCaseAdapter
    private lateinit var videoEncoderConfig: VideoEncoderConfig
    private lateinit var videoEncoder: EncoderImpl
    private lateinit var videoEncoderCallback: EncoderCallback
    private lateinit var previewForVideoEncoder: Preview
    private lateinit var preview: Preview
    private lateinit var mainExecutor: Executor
    private lateinit var encoderExecutor: Executor
    private lateinit var latestSurfaceReadyToRelease: ResolvableFuture<Void>

    @Before
    fun setUp() {
        assumeTrue(CameraUtil.hasCameraWithLensFacing(CameraSelector.LENS_FACING_BACK))
        // Same issue happened in new video encoder in pre-submit test. Bypass this test on
        // CuttleFish API 29.
        // TODO(b/168175357): Fix VideoCaptureTest problems on CuttleFish API 29
        assumeFalse(
            "Cuttlefish has MediaCodec dequeueInput/Output buffer fails issue. Unable to test.",
            Build.MODEL.contains("Cuttlefish") && Build.VERSION.SDK_INT == 29
        )

        val cameraXConfig: CameraXConfig = Camera2Config.defaultConfig()
        CameraX.initialize(context, cameraXConfig).get()

        mainExecutor = ContextCompat.getMainExecutor(context)
        encoderExecutor = CameraXExecutors.ioExecutor()

        previewForVideoEncoder = Preview.Builder().build()
        // Binding one more preview use case to create a surface texture, this is for testing on
        // Pixel API 26, it needs a surface texture at least.
        preview = Preview.Builder().build()

        camera = CameraUtil.createCameraAndAttachUseCase(
            context,
            cameraSelector,
            previewForVideoEncoder,
            preview
        )

        initVideoEncoder()

        instrumentation.runOnMainSync {
            preview.setSurfaceProvider(
                getSurfaceProvider()
            )
        }
    }

    @After
    fun tearDown() {
        if (::camera.isInitialized) {
            camera.apply {
                instrumentation.runOnMainSync {
                    removeUseCases(setOf(previewForVideoEncoder, preview))
                }
            }
        }

        if (::latestSurfaceReadyToRelease.isInitialized) {
            latestSurfaceReadyToRelease.addListener(
                { videoEncoder.release() },
                CameraXExecutors.directExecutor()
            )
        }

        // Ensure all cameras are released for the next test
        CameraX.shutdown()[10, TimeUnit.SECONDS]
    }

    @Test
    fun canRestartVideoEncoder() {
        // Arrange.
        videoEncoder.start()
        var inOrder = inOrder(videoEncoderCallback)
        inOrder.verify(videoEncoderCallback, timeout(5000L)).onEncodeStart()
        inOrder.verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())
        videoEncoder.stopSafely()
        inOrder.verify(videoEncoderCallback, timeout(5000L)).onEncodeStop()
        clearInvocations(videoEncoderCallback)

        // Act.
        videoEncoder.start()

        // Assert.
        inOrder = inOrder(videoEncoderCallback)
        inOrder.verify(videoEncoderCallback, timeout(5000L)).onEncodeStart()
        inOrder.verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())

        // Act.
        videoEncoder.stopSafely()

        // Assert.
        inOrder.verify(videoEncoderCallback, timeout(5000L)).onEncodeStop()
    }

    @Test
    fun canPauseResumeVideoEncoder() {
        videoEncoder.start()

        verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())

        videoEncoder.pause()

        // Since there is no exact event to know the encoder is paused, wait for a while until no
        // callback.
        verify(videoEncoderCallback, noInvocation(3000L, 10000L)).onEncodedData(any())

        clearInvocations(videoEncoderCallback)

        videoEncoder.start()

        verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())
    }

    @Test
    fun canPauseStopStartVideoEncoder() {
        videoEncoder.start()

        verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())

        videoEncoder.pause()

        // Since there is no exact event to know the encoder is paused, wait for a while until no
        // callback.
        verify(videoEncoderCallback, noInvocation(3000L, 10000L)).onEncodedData(any())

        videoEncoder.stopSafely()

        verify(videoEncoderCallback, timeout(5000L)).onEncodeStop()

        clearInvocations(videoEncoderCallback)

        videoEncoder.start()

        verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())
    }

    @Test
    fun pauseResumeVideoEncoder_getChronologicalData() {
        val dataList = ArrayList<EncodedData>()

        videoEncoder.start()
        verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())

        videoEncoder.pause()
        verify(videoEncoderCallback, noInvocation(2000L, 10000L)).onEncodedData(any())

        // Save all values before clear invocations
        val startCaptor = ArgumentCaptor.forClass(EncodedData::class.java)
        verify(videoEncoderCallback, atLeastOnce()).onEncodedData(startCaptor.capture())
        dataList.addAll(startCaptor.allValues)
        clearInvocations(videoEncoderCallback)

        videoEncoder.start()
        val resumeCaptor = ArgumentCaptor.forClass(EncodedData::class.java)
        verify(
            videoEncoderCallback,
            timeout(15000L).atLeast(5)
        ).onEncodedData(resumeCaptor.capture())
        dataList.addAll(resumeCaptor.allValues)

        verifyDataInChronologicalOrder(dataList)
    }

    @Test
    fun startVideoEncoder_firstEncodedDataIsKeyFrame() {
        clearInvocations(videoEncoderCallback)

        videoEncoder.start()
        val captor = ArgumentCaptor.forClass(EncodedData::class.java)
        verify(
            videoEncoderCallback,
            timeout(5000L).atLeastOnce()
        ).onEncodedData(captor.capture())

        assertThat(isKeyFrame(captor.allValues.first().bufferInfo)).isTrue()

        videoEncoder.stopSafely()

        verify(videoEncoderCallback, timeout(5000L)).onEncodeStop()
    }

    @Test
    fun resumeVideoEncoder_firstEncodedDataIsKeyFrame() {
        videoEncoder.start()
        verify(videoEncoderCallback, timeout(15000L).atLeast(5)).onEncodedData(any())

        videoEncoder.pause()
        verify(videoEncoderCallback, noInvocation(2000L, 10000L)).onEncodedData(any())

        clearInvocations(videoEncoderCallback)

        videoEncoder.start()
        val captor = ArgumentCaptor.forClass(EncodedData::class.java)
        verify(videoEncoderCallback, timeout(15000L).atLeastOnce()).onEncodedData(captor.capture())

        assertThat(isKeyFrame(captor.allValues.first().bufferInfo)).isTrue()
    }

    private fun initVideoEncoder() {
        val cameraInfo = camera.cameraInfo as CameraInfoInternal
        val profileProvider = cameraInfo.camcorderProfileProvider

        val profile: CamcorderProfileProxy = when {
            profileProvider.hasProfile(CamcorderProfile.QUALITY_480P) -> {
                profileProvider.get(CamcorderProfile.QUALITY_480P)!!
            }
            profileProvider.hasProfile(CamcorderProfile.QUALITY_LOW) -> {
                profileProvider.get(CamcorderProfile.QUALITY_LOW)!!
            }
            else -> {
                throw AssumptionViolatedException("No available CamcorderProfile")
            }
        }
        Log.d(TAG, "Selected profile = $profile")

        videoEncoderConfig = VideoEncoderConfig.builder()
            .setBitrate(profile.videoBitRate)
            .setColorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            .setFrameRate(profile.videoFrameRate)
            .setIFrameInterval(1)
            .setMimeType(getMimeTypeString(profile.videoCodec))
            .setResolution(Size(profile.videoFrameWidth, profile.videoFrameHeight))
            .build()

        // init video encoder
        videoEncoderCallback = mock(EncoderCallback::class.java)
        doAnswer { args: InvocationOnMock ->
            val encodedData: EncodedData = args.getArgument(0)
            encodedData.close()
            null
        }.`when`(videoEncoderCallback).onEncodedData(any())

        videoEncoder = EncoderImpl(
            encoderExecutor,
            videoEncoderConfig
        )

        videoEncoder.setEncoderCallback(videoEncoderCallback, CameraXExecutors.directExecutor())

        latestSurfaceReadyToRelease = ResolvableFuture.create<Void>().apply { set(null) }

        (videoEncoder.input as Encoder.SurfaceInput).setOnSurfaceUpdateListener(
            mainExecutor
        ) { surface: Surface ->
            latestSurfaceReadyToRelease = ResolvableFuture.create()
            currentSurface = surface
            setVideoPreviewSurfaceProvider(surface)
        }
    }

    private fun setVideoPreviewSurfaceProvider(surface: Surface) {
        previewForVideoEncoder.setSurfaceProvider { request: SurfaceRequest ->
            request.provideSurface(
                surface,
                mainExecutor
            ) {
                if (it.surface != currentSurface) {
                    it.surface.release()
                } else {
                    latestSurfaceReadyToRelease.set(null)
                }
            }
        }
    }

    private fun getSurfaceProvider(): SurfaceProvider {
        return SurfaceTextureProvider.createSurfaceTextureProvider(object : SurfaceTextureCallback {
            override fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture, resolution: Size) {
                // No-op
            }

            override fun onSafeToRelease(surfaceTexture: SurfaceTexture) {
                surfaceTexture.release()
            }
        })
    }

    private fun getMimeTypeString(encoder: Int): String {
        return when (encoder) {
            MediaRecorder.VideoEncoder.H263 -> MediaFormat.MIMETYPE_VIDEO_H263
            MediaRecorder.VideoEncoder.H264 -> MediaFormat.MIMETYPE_VIDEO_AVC
            MediaRecorder.VideoEncoder.HEVC -> MediaFormat.MIMETYPE_VIDEO_HEVC
            MediaRecorder.VideoEncoder.MPEG_4_SP -> MediaFormat.MIMETYPE_VIDEO_MPEG4
            MediaRecorder.VideoEncoder.VP8 -> MediaFormat.MIMETYPE_VIDEO_VP8
            MediaRecorder.VideoEncoder.DEFAULT -> MediaFormat.MIMETYPE_VIDEO_AVC
            else -> MediaFormat.MIMETYPE_VIDEO_AVC
        }
    }

    private fun verifyDataInChronologicalOrder(encodedDataList: List<EncodedData>) {
        // For each item indexed by n and n+1, verify that the timestamp of n is less than n+1.
        encodedDataList.take(encodedDataList.size - 1).forEachIndexed { index, _ ->
            assertThat(encodedDataList[index].presentationTimeUs)
                .isLessThan(encodedDataList[index + 1].presentationTimeUs)
        }
    }

    private fun isKeyFrame(bufferInfo: MediaCodec.BufferInfo): Boolean {
        return bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
    }

    /**
     * Stops safely by first removing the Encoder surface from camera repeating request.
     *
     * <p>As described in b/196039619, when encoder is started and repeating request is running,
     * stop the encoder will get EGL error on some Samsung devices. The encoder surface needs to
     * be removed from repeating request before stop the encoder to avoid this failure.
     */
    private fun EncoderImpl.stopSafely() {
        val deactivateSurfaceBeforeStop =
            DeviceQuirks.get(DeactivateEncoderSurfaceBeforeStopEncoderQuirk::class.java) != null

        if (deactivateSurfaceBeforeStop) {
            instrumentation.runOnMainSync { previewForVideoEncoder.setSurfaceProvider(null) }
            verify(videoEncoderCallback, noInvocation(2000L, 6000L)).onEncodedData(any())
        }

        stop()

        if (deactivateSurfaceBeforeStop && Build.VERSION.SDK_INT >= 23) {
            // The SurfaceProvider needs to be added back to recover repeating. However, for
            // API < 23, EncoderImpl will trigger a surface update event to OnSurfaceUpdateListener
            // and this will be handled by initVideoEncoder() to set the SurfaceProvider with new
            // surface. So no need to add the SurfaceProvider back here.
            instrumentation.runOnMainSync { setVideoPreviewSurfaceProvider(currentSurface!!) }
        }
    }
}
