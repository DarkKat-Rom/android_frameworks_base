/*
 * Copyright (C) 2018 DarkKat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.keyguard.darkkat;

import android.content.Context;
import android.util.AttributeSet;

import com.android.internal.util.darkkat.LockscreenHelper;
import com.android.internal.util.darkkat.WeatherServiceController;
import com.android.internal.util.darkkat.WeatherServiceController.DayForecast;
import com.android.keyguard.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class KeyguardWeatherWidgetDayForecast extends KeyguardWeatherWidget {

    public KeyguardWeatherWidgetDayForecast(Context context) {
        this(context, null);
    }

    public KeyguardWeatherWidgetDayForecast(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardWeatherWidgetDayForecast(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mShow = LockscreenHelper.showWidgets(getContext())
                && LockscreenHelper.showWeatherWidgetDayForecast(getContext());
    }

    @Override
    public void setWidgetVisibility(boolean showing) {
        boolean show = LockscreenHelper.showWidgets(getContext())
                && LockscreenHelper.showWeatherWidgetDayForecast(getContext());
        setWidgetVisibilityInternal(show && showing);
    }

    @Override
    public void onWeatherChanged(WeatherServiceController.WeatherInfo info) {
        if (info != null && info.forecasts != null && info.forecasts.size() > 0) {
            int forecastsSize = info.forecasts.size() > NUM_MAX_CHILDS
                    ? NUM_MAX_CHILDS : info.forecasts.size();
            if (mViewHolders.size() != forecastsSize) {
                mViewHolders = new ArrayList<ViewHolder>();
                if (getChildCount() > 0) {
                    removeAllViews();
                }
                for (int i = 0; i < forecastsSize; i++) {
                    ViewHolder vh = new ViewHolder(this, getWidth() / forecastsSize, true);
                    mViewHolders.add(vh);
                }
            }

            TimeZone myTimezone = TimeZone.getDefault();
            Calendar calendar = new GregorianCalendar(myTimezone);
            for (int i = 0; i < mViewHolders.size(); i++) {
                ViewHolder vh = mViewHolders.get(i);
                DayForecast d = info.forecasts.get(i);
                String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
                        Locale.getDefault());
                String dayTemps = d.formattedTemperatureLow + " | " + d.formattedTemperatureHigh;
                vh.updateContent(dayName, d.conditionDrawableMonochrome, dayTemps);
                vh.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
                calendar.roll(Calendar.DAY_OF_WEEK, true);
            }
        }
    }
}
