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
import android.provider.Settings;

public class NavigationBarColorHelper {

    public static boolean iconColorForRipple(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.NAVIGATION_BAR_ICON_COLOR_FOR_RIPPLE, 1) == 1;
    }

    public static int getIconColorLightMode(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.NAVIGATION_BAR_ICON_COLOR_LIGHT_MODE, ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getRippleColorLightMode(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.NAVIGATION_BAR_RIPPLE_COLOR_LIGHT_MODE, ColorConstants.WHITE);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }


    public static int getIconColorDarkMode(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.NAVIGATION_BAR_ICON_COLOR_DARK_MODE, ColorConstants.BLACK);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }

    public static int getRippleColorDarkMode(Context context) {
        int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.NAVIGATION_BAR_RIPPLE_COLOR_DARK_MODE, ColorConstants.BLACK);
        return (ColorConstants.FULLY_OPAQUE_ALPHA << 24) | (color & 0x00ffffff);
    }
}
