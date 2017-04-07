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

package com.android.systemui.darkkat.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.provider.Settings;

import com.android.internal.util.darkkat.ColorConstants;
import com.android.internal.util.darkkat.ColorHelper;
import com.android.internal.util.darkkat.NotificationColorHelper;
import com.android.internal.util.darkkat.ThemeHelper;

public class NotifColorHelper extends NotificationColorHelper {

    public static int getDimmedBackgroundColor(int primaryBgColor) {
        return (ColorConstants.NOTIFICATION_BG_DIMMED_ALPHA << 24)
                | (primaryBgColor & 0x00ffffff);
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

    public static int getEmphazisedActionBackgroundColor(int actionListBgColor, boolean oddAction) {
        int color;
        if (oddAction) {
            color = actionListBgColor;
        } else {
            color = ColorHelper.getLightenOrDarkenColor(actionListBgColor);
        }

        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getMediaTextColor(Context context, int notificationColor, boolean isPrimary) {
        int alpha;
        int color;

        if (isPrimary) {
            alpha = ColorHelper.isColorDark(notificationColor)
                    ? ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT : ColorConstants.TEXT_PRIMARY_ALPHA_DAY;
        } else {
            alpha = ColorHelper.isColorDark(notificationColor)
                    ? ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT : ColorConstants.TEXT_SECONDARY_ALPHA_DAY;
        }
        if (ColorHelper.isColorDark(notificationColor)) {
            color = ColorConstants.WHITE;
        } else {
            color = ColorConstants.BLACK;
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static ColorStateList getRemoteInputTextColorList(int notificationColor) {
        int alphaEnabled;
        int alphaDisabled;
        int color;

        if (ColorHelper.isColorDark(notificationColor)) {
            alphaEnabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_TEXT_ENABLED_DARK;
            alphaDisabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_TEXT_DISABLED_DARK;
        } else {
            alphaEnabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_TEXT_ENABLED_LIGHT;
            alphaDisabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_TEXT_DISABLED_LIGHT;
        }
        if (ColorHelper.isColorDark(notificationColor)) {
            color = ColorConstants.WHITE;
        } else {
            color = ColorConstants.BLACK;
        }

        int states[][] = new int[][] {
            new int[] { android.R.attr.state_enabled },
            new int[]{}
        };
        int colors[] = new int[] {
            (alphaEnabled << 24) | (color & 0x00ffffff),
            (alphaDisabled << 24) | (color & 0x00ffffff)
        };
        return new ColorStateList(states, colors);
    }

    public static int getRemoteInputHintTextColor(int notificationColor) {
        int alpha;
        int color;

        if (ColorHelper.isColorDark(notificationColor)) {
            alpha = ColorConstants.NOTIFICATION_REMOTE_INPUT_TEXT_DISABLED_DARK;
        } else {
            alpha = ColorConstants.NOTIFICATION_REMOTE_INPUT_TEXT_DISABLED_LIGHT;
        }
        if (ColorHelper.isColorDark(notificationColor)) {
            color = ColorConstants.WHITE;
        } else {
            color = ColorConstants.BLACK;
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static int getMediaIconColor(Context context, int notificationColor, boolean fullyOpaque) {
        int alpha;
        int color;

        if (fullyOpaque) {
            alpha = ColorConstants.FULLY_OPAQUE_ALPHA;
        } else {
            alpha = ColorHelper.isColorDark(notificationColor)
                    ? ColorConstants.ICON_NORMAL_ALPHA_NIGHT : ColorConstants.ICON_NORMAL_ALPHA_DAY;
        }
        if (ColorHelper.isColorDark(notificationColor)) {
            color = ColorConstants.WHITE;
        } else {
            color = ColorConstants.BLACK;
        }

        return (alpha << 24) | (color & 0x00ffffff);
    }

    public static ColorStateList getProgressColorList(Context context, int notificationColor) {
        int color;

        if (notificationColor == 0) {
            color = getIconColor(context, true);
        } else {
            color = getMediaIconColor(context, notificationColor, true);
        }

        return ColorStateList.valueOf(color);
    }

    public static ColorStateList getIconTint(Context context, boolean fullyOpaque) {
        return ColorStateList.valueOf(getIconColor(context, fullyOpaque));
    }

    public static ColorStateList getRemoteInputIconColorList(int notificationColor) {
        int alphaEnabled;
        int alphaDisabled;
        int color;

        if (ColorHelper.isColorDark(notificationColor)) {
            alphaEnabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_ICON_ENABLED_DARK;
            alphaDisabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_ICON_DISABLED_DARK;
        } else {
            alphaEnabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_ICON_ENABLED_LIGHT;
            alphaDisabled = ColorConstants.NOTIFICATION_REMOTE_INPUT_ICON_DISABLED_LIGHT;
        }
        if (ColorHelper.isColorDark(notificationColor)) {
            color = ColorConstants.WHITE;
        } else {
            color = ColorConstants.BLACK;
        }

        int states[][] = new int[][] {
            new int[] { android.R.attr.state_enabled },
            new int[]{}
        };
        int colors[] = new int[] {
            (alphaEnabled << 24) | (color & 0x00ffffff),
            (alphaDisabled << 24) | (color & 0x00ffffff)
        };
        return new ColorStateList(states, colors);
    }

    public static ColorStateList getGutsIconDisabledTint(Context context) {
        int alpha = ColorHelper.isColorDark(getSecondaryBackgroundColor(context))
                ? ColorConstants.NOTIFICATION_REMOTE_INPUT_ICON_DISABLED_DARK
                : ColorConstants.NOTIFICATION_REMOTE_INPUT_ICON_DISABLED_LIGHT;
        int color = getIconColor(context, true);
        return ColorStateList.valueOf((alpha << 24) | (color & 0x00ffffff));
    }

    public static ColorStateList getGutsTickMarkTint(Context context) {
        int color = ColorHelper.isColorDark(getSecondaryBackgroundColor(context))
                ? ColorConstants.WHITE
                : ColorConstants.BLACK;
        return ColorStateList.valueOf(color);
    }

    public static ColorStateList getGutsRadioButtonTint(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            getIconColor(context, false)
        };
        return new ColorStateList(states, colors);
    }
}
