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
import android.content.res.ColorStateList;
import android.provider.Settings;

public class DetailedWeatherColorHelper {

    public static int getAccentColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_ACCENT_COLOR,
                DetailedWeatherThemeHelper.getAccentColor(context));
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getStatusBarBgColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_STATUS_BAR_BG_COLOR,
                DetailedWeatherThemeHelper.getStatusBarBgColor());
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getActionBarBgColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_ACTION_BAR_BG_COLOR,
                DetailedWeatherThemeHelper.getActionBarBgColor());
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getContentBgColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_CONTENT_BG_COLOR,
                DetailedWeatherThemeHelper.getColorBackground(context));
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getCardBgColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_CARD_BG_COLOR,
                DetailedWeatherThemeHelper.getColorBackgroundFloating(context));
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getActionBarPrimaryTextColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_ACTION_BAR_TEXT_COLOR,
                DetailedWeatherThemeHelper.getActionBarTextColor());
        return (ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT << 24) | (color & 0x00ffffff);
    }

    public static int getActionBarSecondaryTextColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_ACTION_BAR_TEXT_COLOR,
                DetailedWeatherThemeHelper.getActionBarTextColor());
        return (ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT << 24) | (color & 0x00ffffff);
    }

    public static ColorStateList getActionBarTabTextColors(Context context) {
        int states[][] = new int[][] {
            new int[] { com.android.internal.R.attr.state_selected },
            new int[]{}
        };
        int colors[] = new int[] {
            getActionBarPrimaryTextColor(context),
            getActionBarSecondaryTextColor(context)
        };

        return new ColorStateList(states, colors);
    }

    public static int getCardPrimaryTextColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_CARD_TEXT_COLOR,
                DetailedWeatherThemeHelper.getPrimaryTextColor(context));
        return (getTextColorAlpha(context, true) << 24) | (color & 0x00ffffff);
    }

    public static int getCardSecondaryTextColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_CARD_TEXT_COLOR,
                DetailedWeatherThemeHelper.getSecondaryTextColor(context));
        return (getTextColorAlpha(context, false) << 24) | (color & 0x00ffffff);
    }

    public static int getActionBarIconColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_ACTION_BAR_ICON_COLOR,
                DetailedWeatherThemeHelper.getActionBarIconColor());
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getCardIconColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_CARD_ICON_COLOR,
                DetailedWeatherThemeHelper.getIconColor(context));
        int alpha;

        if (ThemeHelper.getTheme(context) != ThemeHelper.THEME_MATERIAL_LIGHT) {
            alpha = ColorConstants.ICON_NORMAL_ALPHA_NIGHT;
        } else {
            alpha = ColorConstants.ICON_NORMAL_ALPHA_DAY;
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static int getConditionImageColor(Context context) {
        if (WeatherHelper.getDetailedWeatherConditionIconType(context)
                != WeatherHelper.ICON_MONOCHROME) {
            return 0;
        } else {
            return getCardIconColor(context);
        }
    }

    public static int getActionBarRippleColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_ACTION_BAR_RIPPLE_COLOR,
                DetailedWeatherThemeHelper.getActionBarRippleColor());
        return (ColorConstants.RIPPLE_ALPHA_NIGHT << 24) | (color & 0x00ffffff);
    }

    public static int getCardRippleColor(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_CARD_RIPPLE_COLOR,
                DetailedWeatherThemeHelper.getRippleColor(context));
        int alpha;

        if (ThemeHelper.getTheme(context) != ThemeHelper.THEME_MATERIAL_LIGHT) {
            alpha = ColorConstants.RIPPLE_ALPHA_NIGHT;
        } else {
            alpha = ColorConstants.RIPPLE_ALPHA_DAY;
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static int getDividerAlpha(Context context) {
        int alpha;
        if (ThemeHelper.getTheme(context) != ThemeHelper.THEME_MATERIAL_LIGHT) {
            alpha = ColorConstants.DIVIDER_ALPHA_NIGHT;
        } else {
            alpha = ColorConstants.DIVIDER_ALPHA_DAY;
        }

        return alpha;
    }

    private static int getTextColorAlpha(Context context, boolean isPrimary) {
        if (ThemeHelper.getTheme(context) != ThemeHelper.THEME_MATERIAL_LIGHT) {
            return isPrimary
                    ? ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT
                    : ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT;
        } else {
            return isPrimary
                    ? ColorConstants.TEXT_PRIMARY_ALPHA_DAY
                    : ColorConstants.TEXT_SECONDARY_ALPHA_DAY;
        }
    }
}
