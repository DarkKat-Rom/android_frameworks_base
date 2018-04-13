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
import com.android.internal.util.darkkat.WeatherServiceController.HourForecast;

import java.util.ArrayList;

public class KeyguardWeatherWidgetHourForecast extends KeyguardWeatherWidget {

    public KeyguardWeatherWidgetHourForecast(Context context) {
        this(context, null);
    }

    public KeyguardWeatherWidgetHourForecast(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardWeatherWidgetHourForecast(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mShow = LockscreenHelper.showWidgets(getContext())
                && LockscreenHelper.showWeatherWidgetHourForecast(getContext());
    }

    @Override
    public void setWidgetVisibility(boolean showing) {
        boolean show = LockscreenHelper.showWidgets(getContext())
                && LockscreenHelper.showWeatherWidgetHourForecast(getContext());
        setWidgetVisibilityInternal(show && showing);
    }

    @Override
    public void onWeatherChanged(WeatherServiceController.WeatherInfo info) {
        if (info != null && info.hourforecasts != null && info.hourforecasts.size() > 0) {
            int hourForecastsSize = info.hourforecasts.size() > NUM_MAX_CHILDS
                    ? NUM_MAX_CHILDS : info.hourforecasts.size();
            if (mViewHolders.size() != hourForecastsSize) {
                mViewHolders = new ArrayList<ViewHolder>();
                if (getChildCount() > 0) {
                    removeAllViews();
                }
                for (int i = 0; i < hourForecastsSize; i++) {
                    ViewHolder vh = new ViewHolder(this, getWidth() / hourForecastsSize);
                    mViewHolders.add(vh);
                }
            }

            for (int i = 0; i < mViewHolders.size(); i++) {
                ViewHolder vh = mViewHolders.get(i);
                HourForecast h = info.hourforecasts.get(i);
                vh.updateContent(h.time, h.conditionDrawableMonochrome, h.formattedTemperature);
                vh.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
            }
        }
    }
}
