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

public class SystemUIThemeOverlayHelper {

    public static final int THEME_OVERLAY_MODE_AUTO_DEFAULT    = 0;
    public static final int THEME_OVERLAY_MODE_AUTO_DARK_LIGHT = 1;
    public static final int THEME_OVERLAY_MODE_CURRENT_OVERLAY = 2;

    public static final String THEME_OVERLAY_DARK_PACKAGE_NAME =
            "com.android.systemui.theme.dark";
    public static final String THEME_OVERLAY_DARKKAT_PACKAGE_NAME =
            "com.android.systemui.theme.darkkat";
    public static final String THEME_OVERLAY_DARKKAT_WHITE_PACKAGE_NAME =
            "com.android.systemui.theme.darkkatwhite";
    public static final String THEME_OVERLAY_DARKKAT_LIGHT_PACKAGE_NAME =
            "com.android.systemui.theme.darkkatlight";
    public static final String THEME_OVERLAY_WHITEOUT_PACKAGE_NAME =
            "com.android.systemui.theme.whiteout";
    public static final String THEME_OVERLAY_DARKKAT_BLACK_PACKAGE_NAME =
            "com.android.systemui.theme.darkkatblack";
    public static final String THEME_OVERLAY_BLACKOUT_PACKAGE_NAME =
            "com.android.systemui.theme.blackout";

    public static final String THEME_OVERLAY_TARGET_PACKAGE_NAME =
            "com.android.systemui";

    public static int getThemeOverlayMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SYSTEMUI_THEME_OVERLAY_MODE, THEME_OVERLAY_MODE_CURRENT_OVERLAY);
    }

    public static String getThemeOverlayPackageName(Context context) {
        int themeOverlay = ThemeOverlayHelper.getThemeOverlay(context);
        String packageName = null;
        switch (themeOverlay) {
            case ThemeOverlayHelper.THEME_OVERLAY_DARK:
                packageName = THEME_OVERLAY_DARK_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT:
                packageName = THEME_OVERLAY_DARKKAT_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT_WHITE:
                packageName = THEME_OVERLAY_DARKKAT_WHITE_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT_LIGHT:
                packageName = THEME_OVERLAY_DARKKAT_LIGHT_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_WHITEOUT:
                packageName = THEME_OVERLAY_WHITEOUT_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT_BLACK:
                packageName = THEME_OVERLAY_DARKKAT_BLACK_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_BLACKOUT:
                packageName = THEME_OVERLAY_BLACKOUT_PACKAGE_NAME;
                break;
            default:
                packageName = ThemeOverlayHelper.THEME_OVERLAY_NONE_PACKAGE_NAME;
                break;
        }
        return packageName;
    }

    public static String getThemeOverlayDarkPackageName(Context context) {
        int themeOverlay = ThemeOverlayHelper.getThemeOverlayAutoDarkTheme(context);
        String packageName = null;
        switch (themeOverlay) {
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT:
                packageName = THEME_OVERLAY_DARKKAT_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT_BLACK:
                packageName = THEME_OVERLAY_DARKKAT_BLACK_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_BLACKOUT:
                packageName = THEME_OVERLAY_BLACKOUT_PACKAGE_NAME;
                break;
            default:
                packageName = THEME_OVERLAY_DARK_PACKAGE_NAME;
                break;
        }
        return packageName;
    }

    public static String getThemeOverlayLightPackageName(Context context) {
        int themeOverlay = ThemeOverlayHelper.getThemeOverlayAutoLightTheme(context);
        String packageName = null;
        switch (themeOverlay) {
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT_WHITE:
                packageName = THEME_OVERLAY_DARKKAT_WHITE_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_DARKKAT_LIGHT:
                packageName = THEME_OVERLAY_DARKKAT_LIGHT_PACKAGE_NAME;
                break;
            case ThemeOverlayHelper.THEME_OVERLAY_WHITEOUT:
                packageName = THEME_OVERLAY_WHITEOUT_PACKAGE_NAME;
                break;
            default:
                packageName = ThemeOverlayHelper.THEME_OVERLAY_NONE_PACKAGE_NAME;
                break;
        }
        return packageName;
    }
}
