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

    public static int getTextColor(Context context, boolean isPrimary) {
        int color;

        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationTextColor(context, isPrimary);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_TEXT_COLOR,
                    ThemeHelper.getNotificationTextColor(context, isPrimary));
        }

        return (ThemeHelper.getNotificationTextAlpha(context, isPrimary) << 24) | (color & 0x00ffffff);
    }

    public static int getIconColor(Context context, boolean fullyOpaque) {
        int alpha;
        int color;

        if (fullyOpaque) {
            alpha = ColorConstants.FULLY_OPAQUE_ALPHA;
        } else {
            alpha = ThemeHelper.getTheme(context) == ThemeHelper.THEME_MATERIAL_LIGHT
                    ? ColorConstants.ICON_NORMAL_ALPHA_DAY : ColorConstants.ICON_NORMAL_ALPHA_NIGHT;
        }
        if (ThemeHelper.notificationUseThemeColors(context)) {
            color = ThemeHelper.getNotificationIconColor(context, fullyOpaque);
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.NOTIFICATION_ICON_COLOR,
                    ThemeHelper.getNotificationIconColor(context, fullyOpaque));
        }

        return (alpha << 24) | (color & 0x00ffffff);
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

/*
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
