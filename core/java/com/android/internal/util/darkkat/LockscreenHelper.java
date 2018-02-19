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

public class LockscreenHelper {

    public static boolean showWeather(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_STATUS_AREA_SHOW_WEATHER, 0) == 1;
    }

    public static boolean showWeatherLocation(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.LOCK_SCREEN_STATUS_AREA_SHOW_WEATHER_LOCATION, 1) == 1;
    }

    public static boolean hideWeatherOnAlarm(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System. LOCK_SCREEN_STATUS_AREA_HIDE_WEATHER_ON_ALARM, 0) == 1;
    }

    public static boolean showWeather(Context context, boolean alarmSet) {
        boolean weatherHiddenByAlarm = alarmSet && hideWeatherOnAlarm(context);
        return showWeather(context) && !weatherHiddenByAlarm;
    }
}
