/*
 * Copyright (C) 2016 The OmniROM Project
 *
 * Copyright (C) 2017 DarkKat
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.android.internal.util.darkkat;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherServiceControllerImpl implements WeatherServiceController {
    private static final String TAG = "WeatherService:WeatherServiceController";
    public static final String PACKAGE_NAME = "net.darkkatrom.dkweather";

    public static final ComponentName COMPONENT_DK_WEATHER = new ComponentName(
            PACKAGE_NAME, PACKAGE_NAME + ".DetailedWeatherActivity");

    private static final Uri WEATHER_URI
            = Uri.parse("content://net.darkkatrom.dkweather.provider/weather");
    private static final Uri SETTINGS_URI
            = Uri.parse("content://net.darkkatrom.dkweather.provider/settings");
    private static final String[] WEATHER_PROJECTION = new String[] {
            "type",
            "city",
            "condition",
            "condition_code",
            "formatted_temperature",
            "temperature_low",
            "temperature_hight",
            "formatted_temperature_low",
            "formatted_temperature_hight",
            "formatted_humidity",
            "formatted_wind",
            "formatted_pressure",
            "formatted_rain1h",
            "formatted_rain3h",
            "formatted_snow1h",
            "formatted_snow3h",
            "time_stamp",
            "sunrise",
            "sunset",
            "dayforecast_condition",
            "dayforecast_condition_code",
            "dayforecast_temperature_low",
            "dayforecast_temperature_high",
            "dayforecast_formatted_temperature_low",
            "dayforecast_formatted_temperature_high",
            "dayforecast_formatted_temperature_morning",
            "dayforecast_formatted_temperature_day",
            "dayforecast_formatted_temperature_evening",
            "dayforecast_formatted_temperature_night",
            "hourforecast_condition",
            "hourforecast_condition_code",
            "hourforecast_formatted_temperature",
            "hourforecast_formatted_humidity",
            "hourforecast_formatted_wind",
            "hourforecast_formatted_pressure",
            "hourforecast_formatted_rain",
            "hourforecast_formatted_snow",
            "hourforecast_day",
            "hourforecast_time"
    };
    private static final String[] SETTINGS_PROJECTION = new String[] {
            "enabled",
            "units"
    };

    private static final boolean DEBUG = false;

    private static final int TYPE_CURRENT_WEATHER = 1;
    private static final int TYPE_DAYFORECAST = 2;

    private final Context mContext;
    private final Handler mHandler;
    private final ContentResolver mResolver;
    private final WeatherObserver mWeatherObserver;

    private ArrayList<Callback> mCallbacks;
    private WeatherInfo mCachedInfo;

    private static final DecimalFormat mNoDigitsFormat = new DecimalFormat("0");
    private boolean mMetric;

    public WeatherServiceControllerImpl(Context context) {
        mContext = context;
        mHandler = new Handler();
        mResolver = mContext.getContentResolver();
        mWeatherObserver = new WeatherObserver(mHandler);

        mCallbacks = new ArrayList<Callback>();
        mCachedInfo = new WeatherInfo();

        if (WeatherHelper.isWeatherAvailable(mContext)) {
            Intent updateIntent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(PACKAGE_NAME, PACKAGE_NAME + ".WeatherService");
            updateIntent.setAction(PACKAGE_NAME + ".ACTION_UPDATE");
            updateIntent.putExtra("force", true);
            mContext.startForegroundService(updateIntent);
            mWeatherObserver.observe();
            queryWeather();
        }
    }

    @Override
    public void addCallback(Callback callback) {
        if (callback == null || mCallbacks.contains(callback)) return;
        if (DEBUG) Log.d(TAG, "addCallback " + callback);
        mCallbacks.add(callback);
        callback.onWeatherChanged(mCachedInfo); // immediately update with current values
    }

    @Override
    public void removeCallback(Callback callback) {
        if (callback == null) return;
        if (DEBUG) Log.d(TAG, "removeCallback " + callback);
        mCallbacks.remove(callback);
    }

    @Override
    public void updateWeather() {
        queryWeather();
        fireCallback();
    }

    @Override
    public WeatherInfo getWeatherInfo() {
        return mCachedInfo;
    }

    public void queryWeather() {
        if (!isServiceEnabled()) {
            return;
        }
        Cursor c = mContext.getContentResolver().query(WEATHER_URI, WEATHER_PROJECTION,
                null, null, null);
        if (c != null) {
            try {
                if (c.getCount() > 0) {
                    List<DayForecast> forecastList = new ArrayList<DayForecast>();
                    List<HourForecast> hourForecastList = new ArrayList<HourForecast>();
                    for (int i = 0; i < c.getCount(); i++) {
                        c.moveToPosition(i);
                        int type = c.getInt(0);
                        if (type == TYPE_CURRENT_WEATHER) {
                            mCachedInfo.city = c.getString(1);
                            mCachedInfo.condition = c.getString(2);
                            mCachedInfo.conditionCode = c.getInt(3);
                            mCachedInfo.conditionDrawableMonochrome = getIcon(mCachedInfo.conditionCode,
                                    WeatherHelper.ICON_MONOCHROME);
                            mCachedInfo.conditionDrawableColored = getIcon(mCachedInfo.conditionCode,
                                    WeatherHelper.ICON_COLORED);
                            mCachedInfo.conditionDrawableVClouds = getIcon(mCachedInfo.conditionCode,
                                    WeatherHelper.ICON_VCLOUDS);
                            mCachedInfo.formattedTemperature = c.getString(4);
                            mCachedInfo.temperatureLow = c.getString(5);
                            mCachedInfo.temperatureHigh = c.getString(6);
                            mCachedInfo.formattedTemperatureLow = c.getString(7);
                            mCachedInfo.formattedTemperatureHigh = c.getString(8);
                            mCachedInfo.formattedHumidity = c.getString(9);
                            mCachedInfo.formattedWind = c.getString(10);
                            mCachedInfo.formattedPressure = c.getString(11);
                            mCachedInfo.formattedRain1H = c.getString(12);
                            mCachedInfo.formattedRain3H = c.getString(13);
                            mCachedInfo.formattedSnow1H = c.getString(14);
                            mCachedInfo.formattedSnow3H = c.getString(15);
                            mCachedInfo.timestamp = c.getString(16);
                            mCachedInfo.sunrise = c.getString(17);
                            mCachedInfo.sunset = c.getString(18);
                        } else if (type == TYPE_DAYFORECAST) {
                            DayForecast day = new DayForecast();
                            day.condition = c.getString(19);
                            day.conditionCode = c.getInt(20);
                            day.conditionDrawableMonochrome = getIcon(day.conditionCode,
                                    WeatherHelper.ICON_MONOCHROME);
                            day.conditionDrawableColored = getIcon(day.conditionCode,
                                    WeatherHelper.ICON_COLORED);
                            day.conditionDrawableVClouds = getIcon(day.conditionCode,
                                    WeatherHelper.ICON_VCLOUDS);
                            day.temperatureLow = c.getString(21);
                            day.temperatureHigh = c.getString(22);
                            day.formattedTemperatureLow = c.getString(23);
                            day.formattedTemperatureHigh = c.getString(24);
                            day.formattedTemperatureMorning = c.getString(25);
                            day.formattedTemperatureDay = c.getString(26);
                            day.formattedTemperatureEvening = c.getString(27);
                            day.formattedTemperatureNight = c.getString(28);
                            forecastList.add(day);
                        } else {
                            HourForecast hour = new HourForecast();
                            hour.condition = c.getString(29);
                            hour.conditionCode = c.getInt(30);
                            hour.conditionDrawableMonochrome = getIcon(hour.conditionCode,
                                    WeatherHelper.ICON_MONOCHROME);
                            hour.conditionDrawableColored = getIcon(hour.conditionCode,
                                    WeatherHelper.ICON_COLORED);
                            hour.conditionDrawableVClouds = getIcon(hour.conditionCode,
                                    WeatherHelper.ICON_VCLOUDS);
                            hour.formattedTemperature = c.getString(31);
                            hour.formattedHumidity = c.getString(32);
                            hour.formattedWind = c.getString(33);
                            hour.formattedPressure = c.getString(34);
                            hour.formattedRain = c.getString(35);
                            hour.formattedSnow = c.getString(36);
                            hour.day = c.getString(37);
                            hour.time = c.getString(38);
                            hourForecastList.add(hour);
                        }
                    }
                    mCachedInfo.forecasts = forecastList;
                    mCachedInfo.hourforecasts = hourForecastList;
                }
            } finally {
                c.close();
            }
        }
        if (DEBUG) Log.d(TAG, "queryWeather " + mCachedInfo);
    }

    public boolean isServiceEnabled() {
        if (!WeatherHelper.isWeatherAvailable(mContext)) {
            return false;
        }
        final Cursor c = mContext.getContentResolver().query(SETTINGS_URI, SETTINGS_PROJECTION,
                null, null, null);
        if (c != null) {
            int count = c.getCount();
            if (count == 1) {
                c.moveToPosition(0);
                boolean enabled = c.getInt(0) == 1;
                return enabled;
            }
        }
        return true;
    }

    private Drawable getIcon(int conditionCode, int iconNameValue) {
        String iconName;

        if (iconNameValue == WeatherHelper.ICON_MONOCHROME) {
            iconName = "weather_";
        } else if (iconNameValue == WeatherHelper.ICON_COLORED) {
            iconName = "weather_color_";
        } else {
            iconName = "weather_vclouds_";
        }

        try {
            Resources resources =
                    mContext.createPackageContext(PACKAGE_NAME, 0).getResources();
            return resources.getDrawable(resources.getIdentifier(iconName + conditionCode,
                    "drawable", PACKAGE_NAME));
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private void fireCallback() {
        for (Callback callback : mCallbacks) {
            callback.onWeatherChanged(mCachedInfo);
        }
    }

    class WeatherObserver extends ContentObserver {
        WeatherObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            mResolver.registerContentObserver(WEATHER_URI, false, this);
        }

        void unobserve() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            queryWeather();
            fireCallback();
        }
    }
}
