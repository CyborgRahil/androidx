#
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# The list of support library modules made available to the platform docs build.
FRAMEWORKS_SUPPORT_JAVA_LIBRARIES := \
    android-support-animatedvectordrawable \
    android-support-annotations \
    android-support-emoji \
    android-support-emoji-appcompat \
    android-support-emoji-bundled \
    android-support-compat \
    android-support-core-ui \
    android-support-core-utils \
    android-support-customtabs \
    android-support-design \
    android-support-dynamic-animation \
    android-support-exifinterface \
    android-support-fragment \
    android-support-heifwriter \
    android-support-media-compat \
    android-support-percent \
    android-support-recommendation \
    android-support-transition \
    android-support-tv-provider \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-gridlayout \
    android-support-v7-mediarouter \
    android-support-v7-palette \
    android-support-v7-preference \
    android-support-v7-recyclerview \
    android-support-v13 \
    android-support-v14-preference \
    android-support-v17-leanback \
    android-support-v17-preference-leanback \
    android-support-vectordrawable \
    android-support-wear

# List of all Design transitive dependencies. Use this instead of android-support-design.
ANDROID_SUPPORT_DESIGN_TARGETS := \
    android-support-design \
    android-support-compat \
    android-support-core-ui \
    android-support-core-utils \
    android-support-fragment \
    android-support-transition \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-v7-recyclerview \
    android-support-design-animation \
    android-support-design-bottomnavigation \
    android-support-design-bottomsheet \
    android-support-design-button \
    android-support-design-canvas \
    android-support-design-card \
    android-support-design-chip \
    android-support-design-circularreveal \
    android-support-design-circularreveal-cardview \
    android-support-design-circularreveal-coordinatorlayout \
    android-support-design-color \
    android-support-design-dialog \
    android-support-design-drawable \
    android-support-design-expandable \
    android-support-design-floatingactionbutton \
    android-support-design-math \
    android-support-design-resources \
    android-support-design-ripple \
    android-support-design-snackbar \
    android-support-design-stateful \
    android-support-design-textfield \
    android-support-design-theme \
    android-support-design-transformation \
    android-support-design-typography \
    android-support-design-widget \
    android-support-design-internal \
    flexbox

# List of all Car transitive dependencies. Use this instead of android-support-car.
ANDROID_SUPPORT_CAR_TARGETS := \
    android-support-car \
    $(ANDROID_SUPPORT_DESIGN_TARGETS) \
    android-support-media-compat
