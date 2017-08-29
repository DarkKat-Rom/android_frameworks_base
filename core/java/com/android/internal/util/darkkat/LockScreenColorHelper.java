/*
* Copyright (C) 2015 DarkKat
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

    public static int getPrimaryTextColor(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_TEXT_COLOR, ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getSecondaryTextColor(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_TEXT_COLOR, ColorConstants.WHITE);
        return (ColorConstants.TEXT_SECONDARY_ALPHA_NIGHT << 24)
                | (getPrimaryTextColor(context) & 0x00ffffff);
    }

    public static int getIconColor(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_ICON_COLOR, ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static ColorStateList getIconTint(Context context) {
        return ColorStateList.valueOf(getIconColor(context));
    }

    public static ColorStateList getNormalIconTint(Context context) {
        int baseColor = getIconColor(context);
        return ColorStateList.valueOf(
                (ColorConstants.ICON_NORMAL_ALPHA_NIGHT << 24) | (baseColor & 0x00ffffff));
    }

    public static int getAmbientDisplayBatteryTextColor(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.AMBIENT_DISPLAY_BATTERY_TEXT_COLOR, ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }
}
