/*
* Copyright (C) 2017 DarkKat
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

import com.android.internal.util.darkkat.ColorConstants;
import com.android.internal.util.darkkat.ColorHelper;
import com.android.internal.util.darkkat.ThemeHelper;

public class VolumeDialogColorHelper extends QSColorHelper {

    public static int getAlternativeTextColor(Context context) {
        int themeColor = 0;

        switch (ThemeHelper.getTheme(context)) {
            case ThemeHelper.THEME_DARKKAT:
            case ThemeHelper.THEME_MATERIAL:
            case ThemeHelper.THEME_MATERIAL_LIGHT:
                themeColor = ColorConstants.VOLUME_DIALOG_INACTIVE_DARK;
                break;
            case ThemeHelper.THEME_BLACKOUT:
                themeColor = ColorConstants.VOLUME_DIALOG_INACTIVE_BLACKOUT;
                break;
        }

        int color = themeColor;

        if (!ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            color = getTextColor(context);
        }

        return color;
    }

    public static ColorStateList getInactiveSliderTintList(Context context) {
        int themeColor = 0;

        switch (ThemeHelper.getTheme(context)) {
            case ThemeHelper.THEME_DARKKAT:
            case ThemeHelper.THEME_MATERIAL:
            case ThemeHelper.THEME_MATERIAL_LIGHT:
                themeColor = ColorConstants.VOLUME_DIALOG_INACTIVE_DARK;
                break;
            case ThemeHelper.THEME_BLACKOUT:
                themeColor = ColorConstants.VOLUME_DIALOG_INACTIVE_BLACKOUT;
                break;
        }

        int color = themeColor;

        if (!ThemeHelper.statusBarExpandedUseThemeColors(context)) {
            int foreground = (ColorConstants.VOLUME_DIALOG_INACTIVE_ALPHA << 24)
                    | (getIconColor(context) & 0x00ffffff);
            color = ColorHelper.compositeColors(foreground, getPrimaryBackgroundColor(context));
        }

        return ColorStateList.valueOf(color);
    }
}
