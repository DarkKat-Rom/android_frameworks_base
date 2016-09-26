/*
 * Copyright (C) 2016 DarkKat
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

package com.android.systemui.darkkat.statusbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.darkkat.WeatherServiceController;

import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;

public class StatusBarWeather extends AlphaOptimizedLinearLayout implements
        WeatherServiceController.Callback {

    private static final int WEATHER_TYPE_TEXT      = 0;
    private static final int WEATHER_TYPE_ICON      = 1;
    private static final int WEATHER_TYPE_TEXT_ICON = 2;

    private WeatherServiceController mWeatherController;

    private TextView mTextView;
    private ImageView mIconView;

    private boolean mShow = false;

    public StatusBarWeather(Context context) {
        this(context, null);
    }

    public StatusBarWeather(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarWeather(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTextView = (TextView) findViewById(R.id.status_bar_weather_text);
        mIconView = (ImageView) findViewById(R.id.status_bar_weather_icon);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mWeatherController != null) {
            mWeatherController.addCallback(this);
        }
    }

    public void setWeatherController(WeatherServiceController wc) {
        mWeatherController = wc;
    }

    public void onDensityOrFontScaleChanged() {
        int startPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.weather_layout_start_padding);
        int iconSize = getContext().getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_icon_size);
        int iconPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.status_bar_icon_padding);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, iconSize);
        setPaddingRelative(startPadding, 0, 0, 0);
        FontSizeUtils.updateFontSize(mTextView, R.dimen.status_bar_clock_size);
        mIconView.setLayoutParams(lp);
        mIconView.setPadding(0, iconPadding, 0, iconPadding);
    }

    @Override
    public void onWeatherChanged(WeatherServiceController.WeatherInfo info) {
        if (info.formattedTemperature != null && info.condition != null) {
            if (mShow) {
                if (getVisibility() != View.VISIBLE) {
                    setVisibility(View.VISIBLE);
                }
            }
            mTextView.setText(info.formattedTemperature);
            Drawable icon = info.conditionDrawableMonochrome.getConstantState().newDrawable();
            mIconView.setImageDrawable(icon);
        } else {
            if (getVisibility() != View.GONE) {
                setVisibility(View.GONE);
            }
            mTextView.setText("");
            mIconView.setImageDrawable(null);
        }

    }

    public void setShow(boolean show) {
        mShow = show;
        if (mShow) {
            if (getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
            }
            if (mWeatherController != null) {
                mWeatherController.addCallback(this);
            }
        } else {
            if (getVisibility() != View.GONE) {
                setVisibility(View.GONE);
            }
            if (mWeatherController != null) {
                mWeatherController.removeCallback(this);
            }
        }
    }

    public void setType(int type) {
        final boolean showText = mShow && type == WEATHER_TYPE_TEXT || type == WEATHER_TYPE_TEXT_ICON;
        final boolean showIcon = mShow && type == WEATHER_TYPE_ICON || type == WEATHER_TYPE_TEXT_ICON;

        mTextView.setVisibility(showText ? View.VISIBLE : View.GONE);
        mIconView.setVisibility(showIcon ? View.VISIBLE : View.GONE);
    }

    public void setTextColor(int color) {
        if (mTextView != null) {
            mTextView.setTextColor(color);
        }
    }

    public void setIconColor(int color) {
        if (mIconView != null) {
            mIconView.setColorFilter(color, Mode.MULTIPLY);
        }
    }

    public boolean shouldShow() {
        return mShow;
    }
}
