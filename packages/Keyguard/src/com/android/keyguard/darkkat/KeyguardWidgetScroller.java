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
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

import java.lang.Exception;
import java.util.ArrayList;

public class KeyguardWidgetScroller extends HorizontalScrollView {
    private static final int NUM_ROOT_WIDGET = 2;

    public static final int TIME_WIDGET    = 0;
    public static final int WEATHER_WIDGET = 1;

    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;

    private int mActiveWidget = TIME_WIDGET;

    private GestureDetector mGestureDetector;

    public interface OnActiveWidgetChangeListener {
        public void onActiveWidgetChanged(int newActiveWidget);
    }

    private ArrayList<OnActiveWidgetChangeListener> mOnActiveWidgetChangeListeners;

    public KeyguardWidgetScroller(Context context) {
        this(context, null);
    }

    public KeyguardWidgetScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardWidgetScroller(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mOnActiveWidgetChangeListeners = new ArrayList<OnActiveWidgetChangeListener>();
        setupListeners();
    }

    public void setupListeners() {
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollX = getScrollX();
                    int width = v.getMeasuredWidth();
                    int currentActiveWidget = ((scrollX + (width / 2)) / width);
                    if (mActiveWidget != currentActiveWidget) {
                        mActiveWidget = currentActiveWidget;
                        for (OnActiveWidgetChangeListener onActiveWidgetChangeListener
                                : mOnActiveWidgetChangeListeners) {
                            onActiveWidgetChangeListener.onActiveWidgetChanged(mActiveWidget);
                        }
                    }
                    int scrollTo = mActiveWidget * width;
                    smoothScrollTo(scrollTo, 0);
                    return true;
                } else {
                    return false;
                }
            }
        });
        mGestureDetector = new GestureDetector(new MyGestureDetector());
    }

    private class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX)
                        > SWIPE_THRESHOLD_VELOCITY) {
                    int width = getMeasuredWidth();
                    int currentActiveWidget = (mActiveWidget < (NUM_ROOT_WIDGET - 1))
                            ? mActiveWidget + 1 : NUM_ROOT_WIDGET - 1;
                    if (mActiveWidget != currentActiveWidget) {
                        mActiveWidget = currentActiveWidget;
                        for (OnActiveWidgetChangeListener onActiveWidgetChangeListener
                                : mOnActiveWidgetChangeListeners) {
                            onActiveWidgetChangeListener.onActiveWidgetChanged(mActiveWidget);
                        }
                    }
                    smoothScrollTo(mActiveWidget * width, 0);
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX)
                        > SWIPE_THRESHOLD_VELOCITY) {
                    int width = getMeasuredWidth();
                    int currentActiveWidget = (mActiveWidget > 0) ? mActiveWidget - 1 : 0;
                    if (mActiveWidget != currentActiveWidget) {
                        mActiveWidget = currentActiveWidget;
                        for (OnActiveWidgetChangeListener onActiveWidgetChangeListener
                                : mOnActiveWidgetChangeListeners) {
                            onActiveWidgetChangeListener.onActiveWidgetChanged(mActiveWidget);
                        }
                    }
                    smoothScrollTo(mActiveWidget * width, 0);
                    return true;
                }
            } catch (Exception e) {
                Log.e("Fling", "There was an error processing the Fling event:" + e.getMessage());
            }
            return false;
        }
    }

    public void setOnActiveWidgetChangeListener(
            OnActiveWidgetChangeListener onActiveWidgetChangeListener) {
        if (onActiveWidgetChangeListener == null
                || mOnActiveWidgetChangeListeners.contains(onActiveWidgetChangeListener)) {
            return;
        }
        mOnActiveWidgetChangeListeners.add(onActiveWidgetChangeListener);
        onActiveWidgetChangeListener.onActiveWidgetChanged(mActiveWidget);
    }

    public void removeOnActiveWidgetChangeListener(
            OnActiveWidgetChangeListener onActiveWidgetChangeListener) {
        if (onActiveWidgetChangeListener == null) {
            return;
        }
        mOnActiveWidgetChangeListeners.remove(onActiveWidgetChangeListener);
    }
}
