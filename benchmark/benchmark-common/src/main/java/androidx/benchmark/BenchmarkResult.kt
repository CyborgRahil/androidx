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

package androidx.benchmark

import androidx.annotation.RestrictTo

/**
 * Data capture from a single benchmark test method run.
 *
 * Each field directly corresponds to JSON output, though not every JSON object may be
 * represented directly here.
 * @suppress
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public data class BenchmarkResult(
    val className: String,
    val testName: String,
    @JvmField // Suppress API lint (using JvmField instead of @Suppress to workaround b/175063229))
    val totalRunTimeNs: Long,
    val metrics: List<MetricResult>,
    val repeatIterations: Int,
    @JvmField // Suppress API lint (using JvmField instead of @Suppress to workaround b/175063229))
    val thermalThrottleSleepSeconds: Long,
    val warmupIterations: Int
) {
    public fun getMetricResult(which: String): MetricResult {
        return metrics.first { it.name == which }
    }
}
