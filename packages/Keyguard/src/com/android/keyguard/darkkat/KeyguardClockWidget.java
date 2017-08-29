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
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextClock;

public class KeyguardClockWidget extends TextClock {

    public KeyguardClockWidget(Context context) {
        super(context);
    }

    public KeyguardClockWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyguardClockWidget(Context context, AttributeSet attrs, int defStyle) {
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
}
