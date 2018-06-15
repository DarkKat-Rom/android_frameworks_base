/*
* Copyright (C) 2018 DarkKat
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

public class ThemeColorHelper {


    public static final int STATUS_BAR_DARKEN_COLOR = 0x30000000;

    public static boolean customizeColors(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.CUSTOMIZE_THEME_COLORS, 0) == 1;
    }

    public static int getPrimaryColor(Context context, int defaultColor) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.THEME_PRIMARY_COLOR, defaultColor);
    }

    public static int getIndexForAccentColor(Context context) {
        if (ThemeHelper.isWhiteoutTheme(context) || ThemeHelper.isBlackoutTheme(context)
                || !customizeColors(context)) {
            return 0;
        } else {
            return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.THEME_ACCENT_COLOR, 0);
        }
    }

    public static boolean colorizeNavigationBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.COLORIZE_NAVIGATION_BAR, 0) == 1;
    }

    public static boolean lightStatusBar(Context context, int defaultPrimaryColor) {
        if (ThemeHelper.isBlackoutTheme(context) || ThemeHelper.isWhiteoutTheme(context)) {
            return ThemeHelper.isWhiteoutTheme(context);
        }
        if (customizeColors(context)) {
            return !ColorHelper.isColorDark(getStatusBarBackgroundColor(context, defaultPrimaryColor));
        } else {
            return !ThemeHelper.isNightMode(context) && ThemeHelper.useLightStatusBar(context);
        }
    }

    public static boolean lightActionBar(Context context, int defaultPrimaryColor) {
        if (ThemeHelper.isBlackoutTheme(context) || ThemeHelper.isWhiteoutTheme(context)) {
            return ThemeHelper.isWhiteoutTheme(context);
        }
        if (customizeColors(context)) {
            return !ColorHelper.isColorDark(getPrimaryColor(context, defaultPrimaryColor));
        } else {
            return !ThemeHelper.isNightMode(context) && ThemeHelper.useLightStatusBar(context);
        }
    }

    public static boolean lightNavigationBar(Context context, int defaultPrimaryColor) {
        if (ThemeHelper.isBlackoutTheme(context) || ThemeHelper.isWhiteoutTheme(context)) {
            return ThemeHelper.isWhiteoutTheme(context);
        }

        if (colorizeNavigationBar(context)) {
            return customizeColors(context)
                    ? !ColorHelper.isColorDark(getNavigationBarBackgroundColor(context, defaultPrimaryColor))
                    : !ColorHelper.isColorDark(defaultPrimaryColor);
        } else {
            return !ThemeHelper.isNightMode(context) && ThemeHelper.useLightNavigationBar(context);
        }
    }

    public static int getStatusBarBackgroundColor(Context context, int defaultPrimaryColor) {
        return ColorHelper.compositeColors(STATUS_BAR_DARKEN_COLOR,
                getPrimaryColor(context, defaultPrimaryColor));
    }

    public static int getNavigationBarBackgroundColor(Context context, int defaultPrimaryColor) {
        int color = 0;
        if (!ThemeHelper.isBlackoutTheme(context)
                && !ThemeHelper.isWhiteoutTheme(context)) {
            if (colorizeNavigationBar(context)) {
                color = customizeColors(context)
                        ? getPrimaryColor(context, defaultPrimaryColor) : defaultPrimaryColor;
            }
        }
        return color;
    }
}
