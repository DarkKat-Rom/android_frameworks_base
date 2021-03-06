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

package com.android.internal.util.darkkat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

public class WeatherHelper {
    public static final int ICON_MONOCHROME = 0;
    public static final int ICON_COLORED    = 1;
    public static final int ICON_VCLOUDS    = 2;

    public static final int PACKAGE_ENABLED  = 0;
    public static final int PACKAGE_DISABLED = 1;
    public static final int PACKAGE_MISSING  = 2;

    public static final String DAY_INDEX = "day_index";

    public static int getWeatherAvailability(Context context) {
        boolean isInstalled = false;
        int availability = PACKAGE_MISSING;

        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(WeatherServiceControllerImpl.PACKAGE_NAME,
                    PackageManager.GET_ACTIVITIES);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            // Do nothing
        }

        if (isInstalled) {
            final int enabledState = pm.getApplicationEnabledSetting(
                    WeatherServiceControllerImpl.PACKAGE_NAME);
            if (enabledState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || enabledState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                availability = PACKAGE_DISABLED;
            } else {
                availability = PACKAGE_ENABLED;
            }
        }
        return availability;
    }

    public static boolean isWeatherAvailable(Context context) {
        return getWeatherAvailability(context)
                == PACKAGE_ENABLED;
    }

    public static Intent getWeatherAppDetailSettingsIntent() {
        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + WeatherServiceControllerImpl.PACKAGE_NAME));
        return i;
    }

    public static Intent getWeatherAppSettingsIntent() {
        Intent settings = new Intent(Intent.ACTION_MAIN)
                .setClassName(WeatherServiceControllerImpl.PACKAGE_NAME,
                WeatherServiceControllerImpl.PACKAGE_NAME + ".activities.AppSettingsActivity");
        return settings;
    }
}
