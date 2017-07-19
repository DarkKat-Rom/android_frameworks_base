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

public class DetailedWeatherThemeHelper extends ThemeHelper {

    public static int getStatusBarBgColor(Context context) {
        int color = 0;
        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.MATERIAL_BLUE_700;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.BLACK;
                break;
        }
        return color;
    }

    public static int getActionBarBgColor(Context context) {
        int color = 0;
        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = ColorConstants.MATERIAL_BLUE_500;
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.BLACKOUT_PRIMARY_COLOR;
                break;
        }
        return color;
    }

    public static int getContentBgColor(Context context) {
        return getColorBackground(context);
    }

    public static int getCardBgColor(Context context) {
        int color = 0;
        switch (getTheme(context)) {
            case THEME_DARKKAT:
            case THEME_MATERIAL:
            case THEME_WHITEOUT:
            case THEME_MATERIAL_LIGHT:
                color = getColorBackgroundFloating(context);
                break;
            case THEME_BLACKOUT:
                color = ColorConstants.BLACKOUT_PRIMARY_COLOR;
                break;
        }
        return color;
    }

    public static int getActionBarTextColor() {
        return (ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT << 24) | (ColorConstants.WHITE & 0x00ffffff);
    }

    public static int getActionBarIconColor() {
        return ColorConstants.WHITE;
    }

    public static int getActionBarRippleColor() {
        return (ColorConstants.RIPPLE_ALPHA_NIGHT << 24) | (ColorConstants.WHITE & 0x00ffffff);
    }
}
