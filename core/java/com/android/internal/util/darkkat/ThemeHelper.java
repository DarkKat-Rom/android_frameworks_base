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
    public static final int THEME_DARKKAT        = 3;
    public static final int THEME_MATERIAL       = 2;
    public static final int THEME_WHITEOUT       = 4;
    public static final int THEME_MATERIAL_LIGHT = 1;
    public static final int THEME_BLACKOUT       = 5;

    public static int getTheme(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.UI_NIGHT_MODE, THEME_DARKKAT);
    }

    public static boolean detailedWeatherUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_USE_THEME_COLORS, 1) == 1;
    }

    public static boolean statusBarExpandedUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.STATUS_BAR_EXPANDED_USE_THEME_COLORS, 1) == 1;
    }

    public static boolean slimRecentsUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.SLIM_RECENTS_USE_THEME_COLORS, 1) == 1;
    }

    // General
    public static int getColorBackground(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
                color = ColorConstants.DARKKAT_BLUE_GREY;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.MATERIAL_GREY_850;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.MATERIAL_GREY_50;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.BLACK;
                break;
        }

        return color;
    }

    public static int getColorBackgroundFloating(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
                color = ColorConstants.DARKKAT_BLUE_BLUE_GREY;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.MATERIAL_GREY_800;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.WHITE;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.BLACK;
                break;
        }

        return color;
    }

    public static int getAccentColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.MATERIAL_DEEP_TEAL_500;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.MATERIAL_DEEP_TEAL_200;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.MATERIAL_BLUE_700;
                break;
        }

        return color;
    }

    public static int getPrimaryTextColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_BLACKOUT:
                color = ColorConstants.WHITE;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = (ColorConstants.TEXT_PRIMARY_ALPHA_DAY << 24)
                        | (ColorConstants.BLACK & 0x00ffffff);
                break;
        }

        return color;
    }

    public static int getSecondaryTextColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_BLACKOUT:
                color = (ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT << 24)
                        | (ColorConstants.WHITE & 0x00ffffff);
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = (ColorConstants.TEXT_SECONDARY_ALPHA_DAY << 24)
                        | (ColorConstants.BLACK & 0x00ffffff);
                break;
        }

        return color;
    }

    public static int getIconColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_BLACKOUT:
                color = (ColorConstants.ICON_NORMAL_ALPHA_NIGHT << 24)
                        | (ColorConstants.WHITE & 0x00ffffff);
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = (ColorConstants.ICON_NORMAL_ALPHA_DAY << 24)
                        | (ColorConstants.BLACK & 0x00ffffff);
                break;
        }

        return color;
    }

    public static int getRippleColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_BLACKOUT:
                color = (ColorConstants.RIPPLE_ALPHA_NIGHT << 24)
                        | (ColorConstants.WHITE & 0x00ffffff);
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = (ColorConstants.RIPPLE_ALPHA_DAY << 24)
                        | (ColorConstants.BLACK & 0x00ffffff);
                break;
        }

        return color;
    }

    public static int getDividerColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_BLACKOUT:
                color = (ColorConstants.DIVIDER_ALPHA_NIGHT << 24)
                        | (ColorConstants.WHITE & 0x00ffffff);
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = (ColorConstants.DIVIDER_ALPHA_DAY << 24)
                        | (ColorConstants.BLACK & 0x00ffffff);
                break;
        }

        return color;
    }

    // SystemUI
    public static int getSystemUIPrimaryColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
                color = ColorConstants.SYSTEMUI_PRIMARY_DARKKAT;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.SYSTEMUI_PRIMARY_NIGHT;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.SYSTEMUI_PRIMARY_DAY;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.SYSTEMUI_PRIMARY_BLACKOUT;
                break;
        }

        return color;
    }

    public static int getSystemUISecondaryColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
                color = ColorConstants.SYSTEMUI_SECONDARY_DARKKAT;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.SYSTEMUI_SECONDARY_NIGHT;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.SYSTEMUI_SECONDARY_DAY;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.SYSTEMUI_SECONDARY_BLACKOUT;
                break;
        }

        return color;
    }

    public static int getSystemUIAccentColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
                color = ColorConstants.SYSTEMUI_ACCENT_DARKKAT;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.SYSTEMUI_ACCENT_NIGHT;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.SYSTEMUI_ACCENT_DAY;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.SYSTEMUI_ACCENT_BLACKOUT;
                break;
        }

        return color;
    }

    public static int getSystemUIRippleColor(Context context) {
        return (ColorConstants.SBE_RIPPLE_ALPHA_LIGHT << 24) | (ColorConstants.WHITE & 0x00ffffff);
    }

    public static int getSystemUIRippleAccentColor(Context context) {
        return (ColorConstants.RIPPLE_ALPHA_COLORED << 24)
                | (getSystemUIAccentColor(context) & 0x00ffffff);
    }

    // Slim recents
    public static int getSlimRecentsPanelBgColor(Context context) {
        int color = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
                color = ColorConstants.DARKKAT_BLUE_GREY;
                break;
            case THEME_MATERIAL:
                color = ColorConstants.MATERIAL_GREY_850;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.WHITE;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.BLACK;
                break;
        }

        return color;
    }

    public static int getSlimRecentsPanelEmptyIconColor(Context context) {
        int color = 0;
        int alpha = 0;

        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_BLACKOUT:
                color = ColorConstants.WHITE;
                alpha = ColorConstants.ICON_NORMAL_ALPHA_NIGHT;
                break;
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.BLACK;
                alpha = ColorConstants.ICON_NORMAL_ALPHA_DAY;
                break;
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }
}
