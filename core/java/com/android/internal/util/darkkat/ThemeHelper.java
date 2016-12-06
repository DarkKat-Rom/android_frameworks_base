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
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.UI_NIGHT_MODE, 2);
    }

    public static boolean detailedWeatherUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.DETAILED_WEATHER_USE_THEME_COLORS, 1) == 1;
    }

    public static boolean statusBarExpandedUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.STATUS_BAR_EXPANDED_USE_THEME_COLORS, 1) == 1;
    }

    public static boolean notificationUseThemeColors(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.NOTIFICATION_USE_THEME_COLORS, 1) == 1;
    }

    // General

    public static int getColorBackground(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.DARKKAT_BLUE_GREY;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.MATERIAL_GREY_50;
        } else {
            return ColorConstants.BLACK;
        }
    }

    public static int getColorBackgroundFloating(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.DARKKAT_BLUE_BLUE_GREY;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.WHITE;
        } else {
            return ColorConstants.BLACK;
        }
    }

    public static int getAccentColor(Context context) {
        if (getTheme(context) != THEME_BLACKOUT) {
            return ColorConstants.MATERIAL_DEEP_TEAL_500;
        } else {
            return ColorConstants.MATERIAL_BLUE_700;
        }
    }

    public static int getPrimaryTextColor(Context context) {
        if (getTheme(context) != THEME_MATERIAL_LIGHT) {
            return ColorConstants.WHITE;
        } else {
            return (ColorConstants.TEXT_PRIMARY_ALPHA_DAY << 24)
                    | (ColorConstants.BLACK & 0x00ffffff);
        }
    }

    public static int getSecondaryTextColor(Context context) {
        if (getTheme(context) != THEME_MATERIAL_LIGHT) {
            return (ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT << 24)
                    | (ColorConstants.WHITE & 0x00ffffff);
        } else {
            return (ColorConstants.TEXT_SECONDARY_ALPHA_DAY << 24)
                    | (ColorConstants.BLACK & 0x00ffffff);
        }
    }

    public static int getIconColor(Context context) {
        if (getTheme(context) != THEME_MATERIAL_LIGHT) {
            return (ColorConstants.ICON_NORMAL_ALPHA_NIGHT << 24)
                    | (ColorConstants.WHITE & 0x00ffffff);
        } else {
            return (ColorConstants.ICON_NORMAL_ALPHA_DAY << 24)
                    | (ColorConstants.BLACK & 0x00ffffff);
        }
    }

    public static int getRippleColor(Context context) {
        if (getTheme(context) != THEME_MATERIAL_LIGHT) {
            return (ColorConstants.RIPPLE_ALPHA_NIGHT << 24)
                    | (ColorConstants.WHITE & 0x00ffffff);
        } else {
            return (ColorConstants.RIPPLE_ALPHA_DAY << 24)
                    | (ColorConstants.BLACK & 0x00ffffff);
        }
    }

    public static int getDividerColor(Context context) {
        if (getTheme(context) != THEME_MATERIAL_LIGHT) {
            return (ColorConstants.DIVIDER_ALPHA_NIGHT << 24)
                    | (ColorConstants.WHITE & 0x00ffffff);
        } else {
            return (ColorConstants.DIVIDER_ALPHA_DAY << 24)
                    | (ColorConstants.BLACK & 0x00ffffff);
        }
    }

    // SystemUI

    public static int getSystemUIPrimaryColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.SYSTEMUI_PRIMARY_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.SYSTEMUI_PRIMARY_DAY;
        } else {
            return ColorConstants.SYSTEMUI_PRIMARY_BLACKOUT;
        }
    }

    public static int getSystemUISecondaryColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.SYSTEMUI_SECONDARY_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.SYSTEMUI_SECONDARY_DAY;
        } else {
            return ColorConstants.SYSTEMUI_SECONDARY_BLACKOUT;
        }
    }

    public static int getSystemUIAccentColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.SYSTEMUI_ACCENT_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.SYSTEMUI_ACCENT_DAY;
        } else {
            return ColorConstants.SYSTEMUI_ACCENT_BLACKOUT;
        }
    }

    public static int getSystemUIRippleColor(Context context) {
        return (ColorConstants.SBE_RIPPLE_ALPHA_LIGHT << 24) | (ColorConstants.WHITE & 0x00ffffff);
    }

    public static int getSystemUIRippleAccentColor(Context context) {
        return (ColorConstants.RIPPLE_ALPHA_COLORED << 24)
                | (getSystemUIAccentColor(context) & 0x00ffffff);
    }

    // Notifications

    public static int getNotificationPrimaryBgColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.NOTIFICATION_BG_PRIMARY_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.NOTIFICATION_BG_PRIMARY_DAY;
        } else {
            return ColorConstants.NOTIFICATION_BG_PRIMARY_BLACKOUT;
        }
    }

    public static int getNotificationLowBgColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.NOTIFICATION_BG_LOW_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.NOTIFICATION_BG_LOW_DAY;
        } else {
            return ColorConstants.NOTIFICATION_BG_LOW_BLACKOUT;
        }
    }

    public static int getNotificationSecondaryBgColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.NOTIFICATION_BG_SECONDARY_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.NOTIFICATION_BG_SECONDARY_DAY;
        } else {
            return ColorConstants.NOTIFICATION_BG_SECONDARY_BLACKOUT;
        }
    }

    public static int getNotificationAccentColor(Context context) {
        if (getTheme(context) == THEME_DARKKAT) {
            return ColorConstants.NOTIFICATION_ACCENT_DARKKAT;
        } else if (getTheme(context) == THEME_MATERIAL_LIGHT) {
            return ColorConstants.NOTIFICATION_ACCENT_DAY;
        } else {
            return ColorConstants.NOTIFICATION_ACCENT_BLACKOUT;
        }
    }

    public static int getNotificationTextColor(Context context) {
        if (getTheme(context) != THEME_MATERIAL_LIGHT) {
            return (ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT << 24)
                | (ColorConstants.WHITE & 0x00ffffff);
        } else {
            return (ColorConstants.TEXT_PRIMARY_ALPHA_DAY << 24)
                | (ColorConstants.BLACK & 0x00ffffff);
        }
    }

    public static int getNotificationDismissAllTextColor() {
        return ColorConstants.WHITE;
    }
}
