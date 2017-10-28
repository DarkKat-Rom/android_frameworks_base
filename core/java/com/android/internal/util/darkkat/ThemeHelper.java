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

    public static boolean themeSupportsOptionalĹightSB(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(
                Context.UI_MODE_SERVICE);
        int dayNightTheme = uiModeManager.getDayNightTheme();
        return dayNightTheme == UiModeManager.MODE_NIGHT_NO
            || dayNightTheme == UiModeManager.MODE_NIGHT_NO_DK_WHITEOUT
            || dayNightTheme == UiModeManager.MODE_NIGHT_NO_DK;
    }

    public static boolean useLightStatusBar(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
            Settings.Secure.USE_LIGHT_STATUS_BAR, 0) == 1;
    }
}
