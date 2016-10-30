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

    public static int getStatusBarBgColor() {
        return ColorConstants.MATERIAL_BLUE_700;
    }

    public static int getActionBarBgColor() {
        return ColorConstants.MATERIAL_BLUE_500;
    }

    public static int getContentBgColor(Context context) {
        return getColorBackground(context);
    }

    public static int getCardBgColor(Context context) {
        return getColorBackgroundFloating(context);
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
