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

import com.android.internal.R;

public class ThemeColorHelper {


    public static final int STATUS_BAR_DARKEN_COLOR = 0x30000000;

    public static boolean customizeColors(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.CUSTOMIZE_THEME_COLORS, 0) == 1;
    }

    public static int getPrimaryColor(Context context, int defaultColor) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.THEME_PRIMARY_COLOR, defaultColor);
    }

    public static int getIndexForAccentColor(Context context) {
        if (ThemeHelper.isWhiteoutTheme(context) || ThemeHelper.isBlackoutTheme(context)
                || !customizeColors(context)) {
            return 0;
        } else {
            return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.THEME_ACCENT_COLOR, 0);
        }
    }

    public static int getThemeOverlayAccentResId(Context context) {
        int index = getIndexForAccentColor(context);

        int resId = 0;
        if (index == 1) {
            resId = R.style.ThemeOverlay_Accent_HoloBlueLight;
        } else if (index == 2) {
            resId = R.style.ThemeOverlay_Accent_MaterialBlueGrey500;
        } else if (index == 3) {
            resId = R.style.ThemeOverlay_Accent_MaterialBlue500;
        } else if (index == 4) {
            resId = R.style.ThemeOverlay_Accent_MaterialLightBlue500;
        } else if (index == 5) {
            resId = R.style.ThemeOverlay_Accent_MaterialCyan500;
        } else if (index == 6) {
            resId = R.style.ThemeOverlay_Accent_MaterialDeepTeal500;
        } else if (index == 7) {
            resId = R.style.ThemeOverlay_Accent_MaterialIndigo500;
        } else if (index == 8) {
            resId = R.style.ThemeOverlay_Accent_MaterialPurple500;
        } else if (index == 9) {
            resId = R.style.ThemeOverlay_Accent_MaterialDeepPurple500;
        } else if (index == 10) {
            resId = R.style.ThemeOverlay_Accent_MaterialPink500;
        } else if (index == 11) {
            resId = R.style.ThemeOverlay_Accent_MaterialOrange500;
        } else if (index == 12) {
            resId = R.style.ThemeOverlay_Accent_MaterialDeepOrange500;
        } else if (index == 13) {
            resId = R.style.ThemeOverlay_Accent_MaterialRed500;
        } else if (index == 14) {
            resId = R.style.ThemeOverlay_Accent_MaterialYellow500;
        } else if (index == 15) {
            resId = R.style.ThemeOverlay_Accent_MaterialAmber500;
        } else if (index == 16) {
            resId = R.style.ThemeOverlay_Accent_MaterialGreen500;
        } else if (index == 17) {
            resId = R.style.ThemeOverlay_Accent_MaterialLightGreen500;
        } else if (index == 18) {
            resId = R.style.ThemeOverlay_Accent_MaterialLime500;
        } else if (index == 19) {
            resId = R.style.ThemeOverlay_Accent_Black;
        } else if (index == 20) {
            resId = R.style.ThemeOverlay_Accent_White;
        } else if (index == 21) {
            resId = R.style.ThemeOverlay_Accent_Blue;
        } else if (index == 22) {
            resId = R.style.ThemeOverlay_Accent_Purple;
        } else if (index == 23) {
            resId = R.style.ThemeOverlay_Accent_Orange;
        } else if (index == 24) {
            resId = R.style.ThemeOverlay_Accent_Red;
        } else if (index == 25) {
            resId = R.style.ThemeOverlay_Accent_Yellow;
        } else if (index == 26) {
            resId = R.style.ThemeOverlay_Accent_Green;
        }
        return resId;
    }

    public static boolean colorizeNavigationBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.COLORIZE_NAVIGATION_BAR, 0) == 1;
    }

    public static boolean lightStatusBar(Context context, int defaultPrimaryColor) {
        if (ThemeHelper.isBlackoutTheme(context) || ThemeHelper.isWhiteoutTheme(context)) {
            return ThemeHelper.isWhiteoutTheme(context);
        }
        if (customizeColors(context)) {
            return !ColorHelper.isColorDark(getStatusBarBackgroundColor(context, defaultPrimaryColor));
        } else {
            return !ThemeHelper.isNightMode(context) && ThemeHelper.useLightStatusBar(context);
        }
    }

    public static boolean lightActionBar(Context context, int defaultPrimaryColor) {
        if (ThemeHelper.isBlackoutTheme(context) || ThemeHelper.isWhiteoutTheme(context)) {
            return ThemeHelper.isWhiteoutTheme(context);
        }
        if (customizeColors(context)) {
            return !ColorHelper.isColorDark(getPrimaryColor(context, defaultPrimaryColor));
        } else {
            return !ThemeHelper.isNightMode(context) && ThemeHelper.useLightStatusBar(context);
        }
    }

    public static boolean lightNavigationBar(Context context, int defaultPrimaryColor) {
        if (ThemeHelper.isBlackoutTheme(context) || ThemeHelper.isWhiteoutTheme(context)) {
            return ThemeHelper.isWhiteoutTheme(context);
        }

        if (colorizeNavigationBar(context)) {
            return customizeColors(context)
                    ? !ColorHelper.isColorDark(getNavigationBarBackgroundColor(context, defaultPrimaryColor))
                    : !ColorHelper.isColorDark(defaultPrimaryColor);
        } else {
            return !ThemeHelper.isNightMode(context) && ThemeHelper.useLightNavigationBar(context);
        }
    }

    public static int getStatusBarBackgroundColor(Context context, int defaultPrimaryColor) {
        return ColorHelper.compositeColors(STATUS_BAR_DARKEN_COLOR,
                getPrimaryColor(context, defaultPrimaryColor));
    }

    public static int getNavigationBarBackgroundColor(Context context, int defaultPrimaryColor) {
        int color = 0;
        if (!ThemeHelper.isBlackoutTheme(context)
                && !ThemeHelper.isWhiteoutTheme(context)) {
            if (colorizeNavigationBar(context)) {
                color = customizeColors(context)
                        ? getPrimaryColor(context, defaultPrimaryColor) : defaultPrimaryColor;
            }
        }
        return color;
    }
}
