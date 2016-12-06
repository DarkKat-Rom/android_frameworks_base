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

public class NotificationColorHelper {

    public static int getPrimaryBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationPrimaryBgColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_PRIMARY_BACKGROUND_COLOR,
                    ThemeHelper.getNotificationPrimaryBgColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getDimmedBackgroundColor(Context context) {
        return (ColorConstants.NOTIFICATION_BG_DIMMED_ALPHA << 24)
                | (getPrimaryBackgroundColor(context) & 0x00ffffff);
    }

    public static int getLowBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationLowBgColor(context);
        } else {
            color = ColorHelper.getLightenOrDarkenColor(getPrimaryBackgroundColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getSecondaryBackgroundColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationSecondaryBgColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_SECONDARY_BACKGROUND_COLOR,
                    ThemeHelper.getNotificationSecondaryBgColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getEmphazisedActionBackgroundColor(Context context) {
        int color = getSecondaryBackgroundColor(context);
        if (ThemeHelper.notificationUseThemeColors(context)) {
            switch (ThemeHelper.getTheme(context)) {
                default:
                case ThemeHelper.THEME_DARKKAT:
                    color = ColorConstants.NOTIFICATION_BG_EMPHASIZED_DARKKAT;
                    break;
                case ThemeHelper.THEME_MATERIAL_LIGHT:
                    color = ColorConstants.NOTIFICATION_BG_EMPHASIZED_DAY;
                    break;
                case ThemeHelper.THEME_BLACKOUT:
                    color = ColorConstants.NOTIFICATION_BG_EMPHASIZED_BLACKOUT;
                    break;
            }
        } else {
            color = ColorHelper.getLightenOrDarkenColor(color);
        }
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getAccentColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationAccentColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_ACCENT_COLOR,
                    ThemeHelper.getNotificationAccentColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getTextColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationTextColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_TEXT_COLOR,
                    ThemeHelper.getNotificationTextColor(context));
        }

        return (getTextAlpha(context) << 24) | (color & 0x00ffffff);
    }

    public static int getDismissAllTextColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationDismissAllTextColor();
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_DISMISS_ALL_COLOR,
                    ThemeHelper.getNotificationDismissAllTextColor());
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getTextAlpha(Context context) {
        int alpha;

        if (ThemeHelper.getTheme(context) != ThemeHelper.THEME_MATERIAL_LIGHT) {
            alpha = ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT;
        } else {
            alpha = ColorConstants.TEXT_PRIMARY_ALPHA_DAY;
        }

        return alpha;
    }

/*
    public static int getIconColor(Context context) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationIconColor(context);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_ICON_COLOR,
                    ThemeHelper.getNotificationIconColor(context));
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getRippleColor(Context context) {
        if (ThemeHelper.notificationUseThemeColors(context)) {
            return ThemeHelper.getNotificationRippleColor(context);
        } else {
            int colorBase = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_RIPPLE_COLOR,
                    ThemeHelper.getNotificationRippleColor(context));
            if (colorBase == ThemeHelper.getNotificationRippleColor(context)) {
                return colorBase;
            } else {
                return (getRippleAlpha(colorBase) << 24) | (colorBase & 0x00ffffff);
            }
        }
    }
*/
}
