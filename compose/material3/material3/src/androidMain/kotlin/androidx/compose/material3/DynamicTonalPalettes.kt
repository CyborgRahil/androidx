/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.material3

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color

/** Dynamic colors in Material. */
@RequiresApi(Build.VERSION_CODES.S)
internal fun dynamicTonalPalettes(context: Context): TonalPalettes =
    TonalPalettes(
        // The neutral tonal palette from the generated dynamic color palettes.
        neutral100 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_0),
        neutral99 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_10),
        neutral95 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_50),
        neutral90 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_100),
        neutral80 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_200),
        neutral70 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_300),
        neutral60 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_400),
        neutral50 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_500),
        neutral40 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_600),
        neutral30 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_700),
        neutral20 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_800),
        neutral10 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_900),
        neutral0 = ColorResourceHelper.getColor(context, android.R.color.system_neutral1_1000),

        // The neutral variant tonal palette, sometimes called "neutral 2",  from the
        // generated dynamic color palettes.
        neutralVariant100 = ColorResourceHelper.getColor(
            context,
            android.R.color.system_neutral2_0
        ),
        neutralVariant99 = ColorResourceHelper.getColor(
            context,
            android.R.color.system_neutral2_10
        ),
        neutralVariant95 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_50
        ),
        neutralVariant90 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_100
        ),
        neutralVariant80 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_200
        ),
        neutralVariant70 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_300
        ),
        neutralVariant60 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_400
        ),
        neutralVariant50 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_500
        ),
        neutralVariant40 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_600
        ),
        neutralVariant30 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_700
        ),
        neutralVariant20 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_800
        ),
        neutralVariant10 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_900
        ),
        neutralVariant0 = ColorResourceHelper.getColor(
            context, android.R.color.system_neutral2_1000
        ),

        // The primary tonal palette from the generated dynamic color palettes.
        primary100 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_0),
        primary99 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_10),
        primary95 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_50),
        primary90 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_100),
        primary80 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_200),
        primary70 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_300),
        primary60 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_400),
        primary50 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_500),
        primary40 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_600),
        primary30 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_700),
        primary20 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_800),
        primary10 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_900),
        primary0 = ColorResourceHelper.getColor(context, android.R.color.system_accent1_1000),

        // The secondary tonal palette from the generated dynamic color palettes.
        secondary100 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_0),
        secondary99 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_10),
        secondary95 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_50),
        secondary90 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_100),
        secondary80 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_200),
        secondary70 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_300),
        secondary60 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_400),
        secondary50 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_500),
        secondary40 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_600),
        secondary30 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_700),
        secondary20 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_800),
        secondary10 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_900),
        secondary0 = ColorResourceHelper.getColor(context, android.R.color.system_accent2_1000),

        // The tertiary tonal palette from the generated dynamic color palettes.
        tertiary100 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_0),
        tertiary99 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_10),
        tertiary95 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_50),
        tertiary90 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_100),
        tertiary80 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_200),
        tertiary70 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_300),
        tertiary60 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_400),
        tertiary50 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_500),
        tertiary40 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_600),
        tertiary30 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_700),
        tertiary20 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_800),
        tertiary10 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_900),
        tertiary0 = ColorResourceHelper.getColor(context, android.R.color.system_accent3_1000),
    )

@RequiresApi(23)
private object ColorResourceHelper {
    @DoNotInline
    fun getColor(context: Context, @ColorRes id: Int): Color {
        return Color(context.resources.getColor(id, context.theme))
    }
}
