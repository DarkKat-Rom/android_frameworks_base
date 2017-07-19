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
import android.graphics.Color;
import android.provider.Settings;

public class SlimRecentsColorHelper {

    public static int getPanelBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.slimRecentsUseThemeColors(context)) {
            color = ThemeHelper.getSlimRecentsPanelBgColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SLIM_RECENTS_PANEL_BG_COLOR,
                    ThemeHelper.getSlimRecentsPanelBgColor(context));
        }

        return (ColorConstants.SLIM_RECENTS_PANEL_BG_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getPanelEmptyIconColor(Context context) {
        int color;
        int alpha = 0;

        if (ThemeHelper.slimRecentsUseThemeColors(context)) {
            color = ThemeHelper.getSlimRecentsPanelEmptyIconColor(context);
            alpha = Color.alpha(color);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SLIM_RECENTS_PANEL_EMPTY_ICON_COLOR,
                    ThemeHelper.getSlimRecentsPanelEmptyIconColor(context));
            switch (ThemeHelper.getTheme(context)) {
                case ThemeHelper.THEME_DARKKAT:
                case ThemeHelper.THEME_BLACKOUT:
                    alpha = ColorConstants.ICON_NORMAL_ALPHA_NIGHT;
                    break;
                case ThemeHelper.THEME_MATERIAL:
                case ThemeHelper.THEME_WHITEOUT:
                case ThemeHelper.THEME_MATERIAL_LIGHT:
                    alpha = ColorConstants.ICON_NORMAL_ALPHA_DAY;
                    break;
            }
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static int getCardBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.slimRecentsUseThemeColors(context)) {
            color = ThemeHelper.getColorBackgroundFloating(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SLIM_RECENTS_CARD_BG_COLOR,
                    ThemeHelper.getColorBackgroundFloating(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getCardRippleColor(Context context) {
        int color;
        int alpha = 0;

        if (ThemeHelper.slimRecentsUseThemeColors(context)) {
            color = ThemeHelper.getRippleColor(context);
            alpha = Color.alpha(color);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SLIM_RECENTS_CARD_RIPPLE_COLOR,
                    ThemeHelper.getRippleColor(context));
            switch (ThemeHelper.getTheme(context)) {
                case ThemeHelper.THEME_DARKKAT:
                case ThemeHelper.THEME_BLACKOUT:
                    alpha = ColorConstants.HIGHTLIGHT_ALPHA_NIGHT;
                    break;
                case ThemeHelper.THEME_MATERIAL:
                case ThemeHelper.THEME_WHITEOUT:
                case ThemeHelper.THEME_MATERIAL_LIGHT:
                    alpha = ColorConstants.HIGHTLIGHT_ALPHA_DAY;
                    break;
            }
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static int getCardHeaderTextColor(Context context) {
        int color;
        int alpha = 0;

        if (ThemeHelper.slimRecentsUseThemeColors(context)) {
            color = ThemeHelper.getPrimaryTextColor(context);
            alpha = Color.alpha(color);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SLIM_RECENTS_CARD_HEADER_TEXT_COLOR,
                    ThemeHelper.getPrimaryTextColor(context));
            switch (ThemeHelper.getTheme(context)) {
                case ThemeHelper.THEME_DARKKAT:
                case ThemeHelper.THEME_BLACKOUT:
                    alpha = ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT;
                    break;
                case ThemeHelper.THEME_MATERIAL:
                case ThemeHelper.THEME_WHITEOUT:
                case ThemeHelper.THEME_MATERIAL_LIGHT:
                    alpha = ColorConstants.TEXT_PRIMARY_ALPHA_DAY;
                    break;
            }
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static int getCardActionIconColor(Context context) {
        int color;
        int alpha = 0;

        if (ThemeHelper.slimRecentsUseThemeColors(context)) {
            color = ThemeHelper.getIconColor(context);
            alpha = Color.alpha(color);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SLIM_RECENTS_CARD_ACTION_ICON_COLOR,
                    ThemeHelper.getIconColor(context));
            switch (ThemeHelper.getTheme(context)) {
                case ThemeHelper.THEME_DARKKAT:
                case ThemeHelper.THEME_BLACKOUT:
                    alpha = ColorConstants.ICON_NORMAL_ALPHA_NIGHT;
                    break;
                case ThemeHelper.THEME_MATERIAL:
                case ThemeHelper.THEME_WHITEOUT:
                case ThemeHelper.THEME_MATERIAL_LIGHT:
                    alpha = ColorConstants.ICON_NORMAL_ALPHA_DAY;
                    break;
            }
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }
}
