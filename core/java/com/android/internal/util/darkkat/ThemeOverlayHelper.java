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
        int nightMode = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.UI_NIGHT_MODE, 1);
        int defaultDayNightTheme = Settings.Secure.getInt(context.getContentResolver(),
                (nightMode == 1 ? Settings.Secure.UI_DAY_THEME : Settings.Secure.UI_NIGHT_THEME), 1);
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.THEME_OVERLAY, defaultDayNightTheme);
    }

    public static int getThemeOverlayAutoDarkTheme(Context context) {
        int defaultTheme = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.UI_NIGHT_THEME, 2);
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.THEME_OVERLAY_AUTO_DARK_THEME, defaultTheme);
    }

    public static int getThemeOverlayAutoLightTheme(Context context) {
        int defaultTheme = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.UI_DAY_THEME, 1);
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.THEME_OVERLAY_AUTO_LIGHT_THEME, defaultTheme);
    }

    public static boolean themeSupportsOptionalĹightSB(Context context) {
        int themeOverlay = getThemeOverlay(context);
        return themeOverlay == THEME_OVERLAY_LIGHT
            || themeOverlay == THEME_OVERLAY_DARKKAT_WHITE
            || themeOverlay == THEME_OVERLAY_DARKKAT_LIGHT;
    }

    public static boolean useLightStatusBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.USE_LIGHT_STATUS_BAR, 0) == 1;
    }

    public static boolean themeSupportsOptionalĹightNB(Context context) {
        int themeOverlay = getThemeOverlay(context);
        return themeOverlay == THEME_OVERLAY_LIGHT
            || themeOverlay == THEME_OVERLAY_DARKKAT_WHITE
            || themeOverlay == THEME_OVERLAY_WHITEOUT
            || themeOverlay == THEME_OVERLAY_DARKKAT_LIGHT;
    }

    public static boolean useLightNavigationBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.USE_LIGHT_NAVIGATION_BAR, 0) == 1;
    }
}
