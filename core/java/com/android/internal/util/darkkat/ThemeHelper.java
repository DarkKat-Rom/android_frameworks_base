/*
* Copyright (C) 2016 DarkKat
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

public class ThemeHelper {

    // Themes
    public static final int THEME_DARKKAT        = 2;
    public static final int THEME_MATERIAL_LIGHT = 1;
    public static final int THEME_BLACKOUT       = 3;

    public static int getTheme(Context context) {
        final int theme = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.UI_NIGHT_MODE, 2);
        return theme;
    }

    public static boolean statusBarExpandedUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.STATUS_BAR_EXPANDED_USE_THEME_COLORS, 1) == 1;
    }

    public static int getSystemUIPrimaryColor(Context context) {
        int color;
        if (getTheme(context) == THEME_DARKKAT) {
            color = ColorConstants.SYSTEMUI_PRIMARY_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            color = ColorConstants.SYSTEMUI_PRIMARY_DAY;
        } else {
            color = ColorConstants.SYSTEMUI_PRIMARY_BLACKOUT;
        }
        return color;
    }

    public static int getSystemUISecondaryColor(Context context) {
        int color;
        if (getTheme(context) == THEME_DARKKAT) {
            color = ColorConstants.SYSTEMUI_SECONDARY_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            color = ColorConstants.SYSTEMUI_SECONDARY_DAY;
        } else {
            color = ColorConstants.SYSTEMUI_SECONDARY_BLACKOUT;
        }
        return color;
    }

    public static int getSystemUIAccentColor(Context context) {
        int color;
        if (getTheme(context) == THEME_DARKKAT) {
            color = ColorConstants.SYSTEMUI_ACCENT_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            color = ColorConstants.SYSTEMUI_ACCENT_DAY;
        } else {
            color = ColorConstants.SYSTEMUI_ACCENT_BLACKOUT;
        }
        return color;
    }

    public static int getSystemUIRippleColor(Context context) {
        return (ColorConstants.RIPPLE_ALPHA_LIGHT << 24) | (ColorConstants.WHITE & 0x00ffffff);
    }

    public static int getSystemUIRippleAccentColor(Context context) {
        return (ColorConstants.RIPPLE_ALPHA_COLORED << 24)
                | (getSystemUIAccentColor(context) & 0x00ffffff);
    }
}
