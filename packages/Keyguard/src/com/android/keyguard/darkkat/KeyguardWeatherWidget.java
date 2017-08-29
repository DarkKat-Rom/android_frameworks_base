/*
 * Copyright (C) 2017 DarkKat
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
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.darkkat.LockScreenColorHelper;
import com.android.internal.util.darkkat.LockscreenHelper;
import com.android.internal.util.darkkat.WeatherServiceController;
import com.android.internal.util.darkkat.WeatherServiceController.DayForecast;
import com.android.keyguard.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class KeyguardWeatherWidget extends LinearLayout implements
        WeatherServiceController.Callback {

    private WeatherServiceController mWeatherController;

    private boolean mShow;

    private ArrayList<DayViewHolder> mDayViewHolders = new ArrayList<DayViewHolder>();

    public KeyguardWeatherWidget(Context context) {
        this(context, null);
    }

    public KeyguardWeatherWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardWeatherWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mShow = LockscreenHelper.showWeatherWidget(context);
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < 5; i++) {
            DayViewHolder holder = new DayViewHolder(inflater);
            mDayViewHolders.add(holder);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            int startEndmargin = context.getResources().getDimensionPixelSize(
                    R.dimen.weather_widget_item_margin_start_end);
            lp.setMarginStart(startEndmargin);
            lp.setMarginEnd(startEndmargin);
            addView(holder.root, lp);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View widgetScroller = ((View) (getParent().getParent()));

        boolean remeasure = false;

        if (widgetScroller instanceof HorizontalScrollView) {
            int widgetScrollerWidth = widgetScroller.getMeasuredWidth();
            if (widgetScrollerWidth > 0) {
                widthMeasureSpec = widgetScrollerWidth;
            } else {
                remeasure = true;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(View.MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));

        if (remeasure) {
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
    }

    public void setWeatherController(WeatherServiceController controller) {
        mWeatherController = controller;
        if (mShow) {
            mWeatherController.addCallback(this);
        } else {
            mWeatherController.removeCallback(this);
        }
    }

    public void setKeyguardVisibility(boolean showing) {
        boolean show = LockscreenHelper.showWeatherWidget(getContext());
        if (mShow != show) {
            mShow = show;
        }
        if (mWeatherController != null) {
            mWeatherController.addCallback(mShow ? this : null);
        }
        int newVisibility = mShow && showing ? View.VISIBLE : View.GONE;
        if (newVisibility != getVisibility()) {
            setVisibility(newVisibility);
        }
    }
        

    @Override
    public void onWeatherChanged(WeatherServiceController.WeatherInfo info) {
        if (info.formattedTemperature != null && info.condition != null) {
            TimeZone myTimezone = TimeZone.getDefault();
            Calendar calendar = new GregorianCalendar(myTimezone);
            for (int i = 0; i < mDayViewHolders.size(); i++) {
                DayViewHolder holder = mDayViewHolders.get(i);
                DayForecast d = info.forecasts.get(i);
                String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
                        Locale.getDefault());
                String dayTemps = d.formattedTemperatureLow + " | " + d.formattedTemperatureHigh;

                holder.day.setText(dayName);
                holder.image.setImageDrawable(d.conditionDrawableMonochrome);
                holder.temp.setText(dayTemps);
                calendar.roll(Calendar.DAY_OF_WEEK, true);
            }
        }

    }

    public void setColors() {
        for (int i = 0; i < mDayViewHolders.size(); i++) {
            DayViewHolder holder = mDayViewHolders.get(i);
            holder.day.setTextColor(LockScreenColorHelper.getPrimaryTextColor(getContext()));
            holder.image.setImageTintList(LockScreenColorHelper.getNormalIconTint(getContext()));
            holder.temp.setTextColor(LockScreenColorHelper.getSecondaryTextColor(getContext()));
        }
    }

    private class DayViewHolder {
        public View root;
        public TextView day;
        public ImageView image;
        public TextView temp;

        public DayViewHolder(LayoutInflater inflater) {
            root = inflater.inflate(R.layout.keyguard_weather_widget_item, null);
            day = (TextView) root.findViewById(R.id.weather_widget_item_day);
            image = (ImageView) root.findViewById(R.id.weather_widget_item_image);
            temp = (TextView) root.findViewById(R.id.weather_widget_item_temp);
        }
    }
}
