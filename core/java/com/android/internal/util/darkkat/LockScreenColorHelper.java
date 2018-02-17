/*
* Copyright (C) 2018 DarkKat
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

public class LockScreenColorHelper {

    public static boolean colorizeVisualizer(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_COLORIZE_VISUALIZER, 0) == 1;
    }

    public static int getPrimaryTextColorDark(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_TEXT_COLOR_DARK, ColorConstants.WHITE);
        return (ColorConstants.TEXT_PRIMARY_ALPHA_NIGHT << 24) | (color & 0x00ffffff);
    }

    public static int getPrimaryTextColorLight(Context context) {
        return getPrimaryTextColorLight(context, false);
    }

    public static int getPrimaryTextColorLight(Context context, boolean isColorPickerPreview) {
        int alpha = isColorPickerPreview
                ? ColorConstants.TEXT_PRIMARY_ALPHA_DAY : ColorConstants.TEXT_PRIMARY_ALPHA_DAY;
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_TEXT_COLOR_LIGHT, ColorConstants.BLACK);
        return (ColorConstants.TEXT_PRIMARY_ALPHA_DAY << 24) | (color & 0x00ffffff);
    }

    public static int getSecondaryTextColorDark(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_TEXT_COLOR_DARK, ColorConstants.WHITE);
        return (ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT << 24)
                | (getPrimaryTextColorDark(context) & 0x00ffffff);
    }

    public static int getSecondaryTextColorLight(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_TEXT_COLOR_LIGHT, ColorConstants.BLACK);
        return (ColorConstants.TEXT_SECONDARY_ALPHA_DAY << 24)
                | (getPrimaryTextColorLight(context) & 0x00ffffff);
    }

    public static int getIconColorDark(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_ICON_COLOR_DARK, ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getIconColorLight(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_ICON_COLOR_LIGHT, ColorConstants.BLACK);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static ColorStateList getIconTintDark(Context context) {
        return ColorStateList.valueOf(getIconColorDark(context));
    }

    public static ColorStateList getIconTintLight(Context context) {
        return ColorStateList.valueOf(getIconColorLight(context));
    }

    public static ColorStateList getNormalIconTintDark(Context context) {
        int baseColor = getIconColorDark(context);
        return ColorStateList.valueOf(
                (ColorConstants.ICON_NORMAL_ALPHA_NIGHT << 24) | (baseColor & 0x00ffffff));
    }

    public static ColorStateList getNormalIconTintLight(Context context) {
        int baseColor = getIconColorLight(context);
        return ColorStateList.valueOf(
                (ColorConstants.ICON_NORMAL_ALPHA_DAY << 24) | (baseColor & 0x00ffffff));
    }
}
