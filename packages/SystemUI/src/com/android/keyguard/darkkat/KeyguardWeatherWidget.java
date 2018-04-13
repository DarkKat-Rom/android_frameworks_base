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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.util.darkkat.WeatherServiceController;
import com.android.internal.util.darkkat.WeatherServiceController.DayForecast;
import com.android.internal.util.darkkat.WeatherServiceController.HourForecast;
import com.android.keyguard.R;

import java.util.ArrayList;

public class KeyguardWeatherWidget extends LinearLayout {
    private static final String TAG = "KeyguardWeatherWidgetToday";

    protected static final int NUM_MAX_CHILDS = 5;

    protected WeatherServiceController mWeatherServiceController;
    protected ArrayList<ViewHolder> mViewHolders = new ArrayList<ViewHolder>();

    protected boolean mShow = false;

    protected int mTextColorPrimary = 0;
    protected int mIconColor = 0;
    protected int mTextColorSecondary = 0;

    public KeyguardWeatherWidget(Context context) {
        this(context, null);
    }

    public KeyguardWeatherWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardWeatherWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    public void setWidgetVisibility(boolean showing) {
    }

    protected void setWidgetVisibilityInternal(boolean showing) {
        if (mShow != showing) {
            mShow = showing;
        }
        int newVisibility = mShow ? View.VISIBLE : View.GONE;
        if (newVisibility != getVisibility()) {
            setVisibility(newVisibility);
        }
    }

    public void setWeatherServiceController(WeatherServiceController weatherServiceController) {
        mWeatherServiceController = weatherServiceController;
    }

    public void onWeatherChanged(WeatherServiceController.WeatherInfo info) {
    }

    public void setColors(int textColorPrimary, int iconColor, int textColorSecondary) {
        mTextColorPrimary = textColorPrimary;
        mIconColor = iconColor;
        mTextColorSecondary = textColorSecondary;
        for (int i = 0; i < mViewHolders.size(); i++) {
            ViewHolder vh = mViewHolders.get(i);
            vh.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
        }
    }

    public class ViewHolder {
        View root;
        TextView itemPrimaryText;
        ImageView itemImage;
        TextView itemSecondaryText;

        public ViewHolder(ViewGroup vg, int width) {
            this(vg, width, false);
        }

        public ViewHolder(ViewGroup vg, int width, boolean needMargin) {
            LayoutInflater inflater =
                    (LayoutInflater) vg.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            root = inflater.inflate(R.layout.keyguard_weather_widget_item, vg, false);

            if (needMargin) {
                int startEndMargin = vg.getContext().getResources().getDimensionPixelSize(
                        R.dimen.weather_widget_item_margin_start_end);

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) root.getLayoutParams();
                lp.width = width - 2 * startEndMargin;
                lp.setMarginStart(startEndMargin);
                lp.setMarginEnd(startEndMargin);
                vg.addView(root, lp);
            } else {
                root.getLayoutParams().width = width;
                vg.addView(root);
            }
            itemPrimaryText = (TextView) root.findViewById(R.id.weather_widget_item_primary);
            itemImage = (ImageView) root.findViewById(R.id.weather_widget_item_image);
            itemSecondaryText = (TextView) root.findViewById(R.id.weather_widget_item_secondary);
        }

        public void updateContent(String primaryText, Drawable image, String secondaryText) {
            itemPrimaryText.setText(primaryText);
            itemImage.setImageDrawable(image);
            itemSecondaryText.setText(secondaryText);
        }

        public void setColors(int textColorPrimary, int iconColor, int textColorSecondary) {
            if (textColorPrimary != 0) {
                itemPrimaryText.setTextColor(textColorPrimary);
            }
            if (iconColor != 0) {
                itemImage.setImageTintList(ColorStateList.valueOf(iconColor));
            }
            if (textColorSecondary != 0) {
                itemSecondaryText.setTextColor(textColorSecondary);
            }
        }
    }
}
