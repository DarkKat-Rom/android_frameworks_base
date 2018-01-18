/*
* Copyright (C) 2017 DarkKat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.internal.util.darkkat;

import android.content.Context;
import android.provider.Settings;

public class ThemeOverlayHelper {

    public static final int THEME_OVERLAY_LIGHT         = 1;
    public static final int THEME_OVERLAY_DARK          = 2;
    public static final int THEME_OVERLAY_DARKKAT       = 3;
    public static final int THEME_OVERLAY_DARKKAT_WHITE = 4;
    public static final int THEME_OVERLAY_DARKKAT_LIGHT = 5;
    public static final int THEME_OVERLAY_WHITEOUT      = 6;
    public static final int THEME_OVERLAY_DARKKAT_BLACK = 7;
    public static final int THEME_OVERLAY_BLACKOUT      = 8;

    public static final String THEME_OVERLAY_NONE_PACKAGE_NAME = "none";

    public static int getThemeOverlay(Context context) {
        return ThemeHelper.getTheme(context);
    }

    public static int getThemeOverlayAutoDarkTheme(Context context) {
        return ThemeHelper.getNightTheme(context);
    }

    public static int getThemeOverlayAutoLightTheme(Context context) {
        return ThemeHelper.getDayTheme(context);
    }
}
