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

public class StatusBarExpandedColorHelper {

    public static int getPrimaryBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = ThemeHelper.getSystemUIPrimaryColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_PRIMARY_BACKGROUND_COLOR,
                    ThemeHelper.getSystemUIPrimaryColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getSecondaryBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = ThemeHelper.getSystemUISecondaryColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_SECONDARY_BACKGROUND_COLOR,
                    ThemeHelper.getSystemUISecondaryColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getAccentColor(Context context) {
        int color;

        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = ThemeHelper.getSystemUIAccentColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_ACCENT_COLOR,
                    ThemeHelper.getSystemUIAccentColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getTextColor(Context context) {
        int color;

        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = ColorConstants.WHITE;
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_TEXT_COLOR,
                    ColorConstants.WHITE);
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getIconColor(Context context) {
        int color;

        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = ColorConstants.WHITE;
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_ICON_COLOR,
                    ColorConstants.WHITE);
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getRippleColor(Context context) {
        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            return ThemeHelper.getSystemUIRippleColor(context);
        } else {
            int colorBase = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_RIPPLE_COLOR,
                    ThemeHelper.getSystemUIRippleColor(context));
            if (colorBase == ThemeHelper.getSystemUIRippleColor(context)) {
                return colorBase;
            } else {
                return (getRippleAlpha(colorBase) << 24) | (colorBase & 0x00ffffff);
            }
        }
    }

    public static int getRippleAccentColor(Context context) {
        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            return ThemeHelper.getSystemUIRippleAccentColor(context);
        } else {
            int colorBase = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_ACCENT_COLOR,
                    ThemeHelper.getSystemUIRippleAccentColor(context));

            if (colorBase == ThemeHelper.getSystemUIRippleAccentColor(context)) {
                return colorBase;
            } else {
                return (getRippleAlpha(colorBase) << 24) | (colorBase & 0x00ffffff);
            }
        }
    }

    protected static int getRippleAlpha(int color) {
        if (ColorHelper.isColorGrayscale(color)) {
            return ColorHelper.isColorDark(color) ?
                   ColorConstants.SBE_RIPPLE_ALPHA_DARK : ColorConstants.SBE_RIPPLE_ALPHA_LIGHT;
        } else {
            return ColorConstants.SBE_RIPPLE_ALPHA_COLORED;
        }
    }

    public static int getBatteryTextColor(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.STATUS_BAR_EXPANDED_BATTERY_TEXT_COLOR,
                ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }
}
