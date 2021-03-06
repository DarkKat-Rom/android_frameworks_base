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

import android.app.UiModeManager;
import android.content.Context;
import android.provider.Settings;

import com.android.internal.R;

public class ThemeHelper {

    public static int getNightMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(
                Context.UI_MODE_SERVICE);
        return uiModeManager.getNightMode();
    }

    public static int getTheme(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(
                Context.UI_MODE_SERVICE);
        return uiModeManager.getDayNightTheme();
    }

    public static int getDayTheme(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.UI_DAY_THEME, UiModeManager.MODE_NIGHT_NO);
    }

    public static int getNightTheme(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.UI_NIGHT_THEME, UiModeManager.MODE_NIGHT_YES);
    }

    public static boolean isBlackoutTheme(Context context) {
        return getTheme(context) == UiModeManager.MODE_NIGHT_YES_BLACKOUT;
    }

    public static boolean isWhiteoutTheme(Context context) {
        return getTheme(context) == UiModeManager.MODE_NIGHT_NO_WHITEOUT;
    }

    public static boolean useLightStatusBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.USE_LIGHT_STATUS_BAR, 0) == 1;
    }

    public static boolean useLightNavigationBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.USE_LIGHT_NAVIGATION_BAR, 0) == 1;
    }

    public static boolean isNightMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(
                Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            int dayNightTheme = uiModeManager.getDayNightTheme();
            return dayNightTheme == UiModeManager.MODE_NIGHT_YES
                || dayNightTheme == UiModeManager.MODE_NIGHT_YES_DK
                || dayNightTheme == UiModeManager.MODE_NIGHT_YES_DK_BLACKOUT
                || dayNightTheme == UiModeManager.MODE_NIGHT_YES_BLACKOUT;
        } else {
            return false;
        }
    }

    public static int getDKThemeResId(Context context) {
        int defaultPrimaryColor = context.getColor(R.color.primary_color_darkkat);
        int resId = 0;
        boolean lightStatusBar = ThemeColorHelper.lightStatusBar(context, defaultPrimaryColor);
        boolean lightActionBar = ThemeColorHelper.lightActionBar(context, defaultPrimaryColor);
        boolean lightNavigationBar = ThemeColorHelper.lightNavigationBar(context, defaultPrimaryColor);

        if (!isBlackoutTheme(context) && !isWhiteoutTheme(context)) {
            if (lightActionBar && lightNavigationBar) {
                resId = lightStatusBar
                        ? R.style.Theme_DarkKat_DayNight_LightStatusBar_LightNavigationBar
                        : R.style.Theme_DarkKat_DayNight_LightActionBar_LightNavigationBar;
            } else if (lightActionBar) {
                resId = lightStatusBar
                        ? R.style.Theme_DarkKat_DayNight_LightStatusBar
                        : R.style.Theme_DarkKat_DayNight_LightActionBar;
            } else if (lightNavigationBar) {
                resId = R.style.Theme_DarkKat_DayNight_LightNavigationBar;
            } else {
                resId = R.style.Theme_DarkKat_DayNight;
            }
        }
        return resId;
    }
}
