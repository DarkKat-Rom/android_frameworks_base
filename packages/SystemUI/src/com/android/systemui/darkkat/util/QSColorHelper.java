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
import com.android.internal.util.darkkat.StatusBarExpandedColorHelper;
import com.android.internal.util.darkkat.ThemeHelper;

public class QSColorHelper extends StatusBarExpandedColorHelper {

    public static ColorStateList getPrimaryBgTintList(Context context) {
        return ColorStateList.valueOf(getPrimaryBackgroundColor(context));
    }

    public static ColorStateList getSecondaryBgTintList(Context context) {
        return ColorStateList.valueOf(getSecondaryBackgroundColor(context));
    }

    public static ColorStateList getZenButtonsBgTintList(Context context) {
        int colorLight = ColorConstants.BLACK;
        int colorDark = ColorConstants.WHITE;
        if (ColorHelper.isColorDark(getPrimaryBackgroundColor(context))) {
            return ColorStateList.valueOf(colorDark);
        } else {
            return ColorStateList.valueOf(colorLight);
        }
    }

    public static ColorStateList getAccentTintList(Context context) {
        return ColorStateList.valueOf(getAccentColor(context));
    }

    public static ColorStateList getUserAvatarFrameTintList(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_activated },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            0x00000000
        };
        return new ColorStateList(states, colors);
    }

    public static int getTextColorSecondary(Context context) {
        return (ColorConstants.TEXT_SECONDARY_ALPHA << 24) | (getTextColor(context) & 0x00ffffff);
    }

    public static int getTextDisabledColor(Context context) {
        return (ColorConstants.QS_TILE_DISABLED_ALPHA << 24) | (getTextColor(context) & 0x00ffffff);
    }

    public static int getTextInactiveColor(Context context) {
        return (ColorConstants.QS_TILE_INACTIVE_ALPHA << 24) | (getTextColor(context) & 0x00ffffff);
    }

    public static int getTextUnavailableColor(Context context) {
        return (ColorConstants.QS_TILE_UNAVAILABLE_ALPHA << 24) | (getTextColor(context) & 0x00ffffff);
    }

    public static ColorStateList getTextDisabledTintList(Context context) {
        return ColorStateList.valueOf(getTextDisabledColor(context));
    }

    public static ColorStateList getLabelTintList(Context context) {
        int states[][] = new int[][] {
            new int[] { -android.R.attr.state_enabled },
            new int[]{}
        };
        int colors[] = new int[] {
            getTextDisabledColor(context),
            getTextColorSecondary(context)
        };
        return new ColorStateList(states, colors);
    }

    public static ColorStateList getZenModeButtonTextColors(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_selected },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            getTextColor(context)
        };

        return new ColorStateList(states, colors);
    }

    public static ColorStateList getUserNameTintList(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_activated },
            new int[] { -android.R.attr.state_enabled },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            getTextDisabledColor(context),
            getTextColorSecondary(context)
        };
        return new ColorStateList(states, colors);
    }

    public static int getIconNormalColor(Context context) {
        return (ColorConstants.ICON_NORMAL_ALPHA << 24) | (getIconColor(context) & 0x00ffffff);
    }

    public static int getIconDisabledColor(Context context) {
        return (ColorConstants.QS_TILE_DISABLED_ALPHA << 24) | (getIconColor(context) & 0x00ffffff);
    }

    public static int getIconInactiveColor(Context context) {
        return (ColorConstants.QS_TILE_INACTIVE_ALPHA << 24) | (getIconColor(context) & 0x00ffffff);
    }

    public static int getIconUnavailableColor(Context context) {
        return (ColorConstants.QS_TILE_UNAVAILABLE_ALPHA << 24) | (getIconColor(context) & 0x00ffffff);
    }

    public static ColorStateList getIconTintList(Context context) {
        return ColorStateList.valueOf(getIconColor(context));
    }

    public static ColorStateList getIconNormalTintList(Context context) {
        return ColorStateList.valueOf(getIconNormalColor(context));
    }

    public static ColorStateList getIconTunerTintList(Context context) {
        int color = (ColorConstants.ICON_TUNER_ALPHA << 24) | (getIconColor(context) & 0x00ffffff);
        return ColorStateList.valueOf(color);
    }

    public static ColorStateList getSwitchThumbTintList(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            getSwitchThumbNormalColor(context)
        };

        return new ColorStateList(states, colors);
    }

    public static ColorStateList getSwitchTrackTintList(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            getSwitchTrackNormalColor(context)
        };

        return new ColorStateList(states, colors);
    }

    public static ColorStateList getZenModeConditionsIconColors(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_checked },
            new int[]{}
        };
        int colors[] = new int[] {
            getAccentColor(context),
            getIconNormalColor(context)
        };

        return new ColorStateList(states, colors);
    }

    private static int getSwitchThumbNormalColor(Context context) {
        int color;
        if (ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = ColorConstants.SYSTEMUI_SWITCH_THUMB_NORMAL;
        } else {
            color = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_ICON_COLOR,
                    ColorConstants.SYSTEMUI_SWITCH_THUMB_NORMAL);
        }
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    private static int getSwitchTrackNormalColor(Context context) {
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

    public static ColorStateList getRippleTintList(Context context) {
        return ColorStateList.valueOf(getRippleColor(context));
    }

    public static ColorStateList getCheckableViewRippleTintList(Context context) {
        int states[][] = new int[][] {
            new int[] { android.R.attr.state_checked, android.R.attr.state_enabled },
            new int[]{}
        };
        int colors[] = new int[] {
            getRippleAccentColor(context),
            getRippleColor(context)
        };

        return new ColorStateList(states, colors);
    }
}
