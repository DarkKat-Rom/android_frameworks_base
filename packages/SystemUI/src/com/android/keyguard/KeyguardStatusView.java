/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.keyguard;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.android.internal.util.ArrayUtils;
import com.android.internal.util.darkkat.LockScreenColorHelper;
import com.android.internal.util.darkkat.LockscreenHelper;
import com.android.internal.util.darkkat.WeatherServiceController;
import com.android.internal.util.darkkat.WeatherServiceController.WeatherInfo;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.darkkat.KeyguardWeatherWidget;
import com.android.keyguard.darkkat.KeyguardWidgetScroller;
import com.android.systemui.statusbar.policy.DateView;

import java.util.Locale;

public class KeyguardStatusView extends GridLayout implements
        WeatherServiceController.Callback, KeyguardWidgetScroller.OnActiveWidgetChangeListener {
    private static final boolean DEBUG = KeyguardConstants.DEBUG;
    private static final String TAG = "KeyguardStatusView";
    private static final int MARQUEE_DELAY_MS = 2000;

    private final LockPatternUtils mLockPatternUtils;
    private final AlarmManager mAlarmManager;

    private TextView mAlarmStatusView;
    private TextView mWeatherStatusView;
    private DateView mDateView;
    private KeyguardWidgetScroller mKeyguardWidgetScroller;
    private TextClock mClockView;
    private KeyguardWeatherWidget mWeatherWidgetHourForecast;
    private KeyguardWeatherWidget mWeatherWidgetDayForecast;
    private TextView mOwnerInfo;
    private ViewGroup mClockContainer;
    private View mKeyguardStatusArea;
    private Runnable mPendingMarqueeStart;
    private Handler mHandler;

    private View[] mVisibleInDoze;
    private boolean mPulsing;
    private float mDarkAmount = 0;
    private int mTextColorPrimary;
    private int mTextColorSecondary;
    private int mIconColor;
    private boolean mLockDarkText = false;

    private int mActiveWidget = KeyguardWidgetScroller.TIME_WIDGET;

    private WeatherInfo mWeatherInfo;
    private WeatherServiceController mWeatherServiceController;

    private KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {

        @Override
        public void onTimeChanged() {
            refresh();
        }

        @Override
        public void onKeyguardVisibilityChanged(boolean showing) {
            if (showing) {
                if (DEBUG) Slog.v(TAG, "refresh statusview showing:" + showing);

                boolean showWidgets = LockscreenHelper.showWidgets(mContext);
                boolean showWeatherWidgetHourForecast =
                        LockscreenHelper.showWeatherWidgetHourForecast(mContext);
                boolean showWeatherWidgetDayForecast =
                        LockscreenHelper.showWeatherWidgetDayForecast(mContext);
                if (showWidgets) {
                    if (showWeatherWidgetHourForecast || showWeatherWidgetDayForecast) {
                        mKeyguardWidgetScroller
                                .setOnActiveWidgetChangeListener(KeyguardStatusView.this);
                    } else {
                        mKeyguardWidgetScroller
                                .removeOnActiveWidgetChangeListener(KeyguardStatusView.this);
                        mActiveWidget = KeyguardWidgetScroller.TIME_WIDGET;
                    }
                } else {
                    mKeyguardWidgetScroller.removeOnActiveWidgetChangeListener(KeyguardStatusView.this);
                    mActiveWidget = KeyguardWidgetScroller.TIME_WIDGET;
                }
                mWeatherWidgetHourForecast.setWidgetVisibility(showing);
                mWeatherWidgetDayForecast.setWidgetVisibility(showing);
                addOnWeatherChangedCallback();
                updateOwnerInfo();
                updateColors();
            } else {
                mKeyguardWidgetScroller.removeOnActiveWidgetChangeListener(KeyguardStatusView.this);
                removeOnWeatherChangedCallback();
            }
        }

        @Override
        public void onStartedWakingUp() {
            setEnableMarquee(true);
        }

        @Override
        public void onFinishedGoingToSleep(int why) {
            setEnableMarquee(false);
        }

        @Override
        public void onUserSwitchComplete(int userId) {
            refresh();
            updateOwnerInfo();
        }
    };

    public KeyguardStatusView(Context context) {
        this(context, null, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mLockPatternUtils = new LockPatternUtils(getContext());
        mHandler = new Handler(Looper.myLooper());
    }

    private void setEnableMarquee(boolean enabled) {
        if (DEBUG) Log.v(TAG, "Schedule setEnableMarquee: " + (enabled ? "Enable" : "Disable"));
        if (enabled) {
            if (mPendingMarqueeStart == null) {
                mPendingMarqueeStart = () -> {
                    setEnableMarqueeImpl(true);
                    mPendingMarqueeStart = null;
                };
                mHandler.postDelayed(mPendingMarqueeStart, MARQUEE_DELAY_MS);
            }
        } else {
            if (mPendingMarqueeStart != null) {
                mHandler.removeCallbacks(mPendingMarqueeStart);
                mPendingMarqueeStart = null;
            }
            setEnableMarqueeImpl(false);
        }
    }

    private void setEnableMarqueeImpl(boolean enabled) {
        if (DEBUG) Log.v(TAG, (enabled ? "Enable" : "Disable") + " transport text marquee");
        if (mAlarmStatusView != null) mAlarmStatusView.setSelected(enabled);
        if (mOwnerInfo != null) mOwnerInfo.setSelected(enabled);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLockDarkText = mContext.getThemeResId() == R.style.Theme_SystemUI_Light;
        mTextColorPrimary = mLockDarkText ? LockScreenColorHelper.getPrimaryTextColorLight(mContext)
                : LockScreenColorHelper.getPrimaryTextColorDark(mContext);
        mTextColorSecondary = mLockDarkText ? LockScreenColorHelper.getSecondaryTextColorLight(mContext)
                : LockScreenColorHelper.getSecondaryTextColorDark(mContext);

        mIconColor = mLockDarkText ? LockScreenColorHelper.getNormalIconColorLight(mContext)
                : LockScreenColorHelper.getNormalIconColorDark(mContext);

        mClockContainer = findViewById(R.id.keyguard_clock_container);
        mAlarmStatusView = findViewById(R.id.alarm_status);
        mAlarmStatusView.setTextColor(mTextColorSecondary);
        mAlarmStatusView.setCompoundDrawableTintList(ColorStateList.valueOf(mTextColorSecondary));
        mWeatherStatusView = findViewById(R.id.weather_status);
        mWeatherStatusView.setTextColor(mTextColorSecondary);
        mDateView = findViewById(R.id.date_view);
        mDateView.setTextColor(mTextColorPrimary);
        mKeyguardWidgetScroller = (KeyguardWidgetScroller) findViewById(R.id.widget_scroller);
        mClockView = findViewById(R.id.clock_view);
        mClockView.setTextColor(mTextColorPrimary);
        mClockView.setShowCurrentUserTime(true);
        if (KeyguardClockAccessibilityDelegate.isNeeded(mContext)) {
            mClockView.setAccessibilityDelegate(new KeyguardClockAccessibilityDelegate(mContext));
        }
        mWeatherWidgetHourForecast =
                (KeyguardWeatherWidget) findViewById(R.id.weather_widget_hour_forecast);
        mWeatherWidgetHourForecast.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
        mWeatherWidgetDayForecast =
                (KeyguardWeatherWidget) findViewById(R.id.weather_widget_day_forecast);
        mWeatherWidgetDayForecast.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
        mOwnerInfo = findViewById(R.id.owner_info);
        mOwnerInfo.setTextColor(mTextColorSecondary);
        mKeyguardStatusArea = findViewById(R.id.keyguard_status_area);
        mVisibleInDoze = new View[]{mKeyguardWidgetScroller, mKeyguardStatusArea};
        boolean shouldMarquee = KeyguardUpdateMonitor.getInstance(mContext).isDeviceInteractive();
        setEnableMarquee(shouldMarquee);
        refresh();
        updateOwnerInfo();

        // Disable elegant text height because our fancy colon makes the ymin value huge for no
        // reason.
        mClockView.setElegantTextHeight(false);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mClockView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.widget_big_font_size));
        // Some layouts like burmese have a different margin for the clock
        MarginLayoutParams layoutParams = (MarginLayoutParams) mClockView.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelSize(
                R.dimen.bottom_text_spacing_digital);
        mClockView.setLayoutParams(layoutParams);
        mDateView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(R.dimen.widget_label_font_size));
        if (mOwnerInfo != null) {
            mOwnerInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(R.dimen.widget_label_font_size));
        }
    }

    @Override
    public void onActiveWidgetChanged(int newActiveWidget) {
        if (mActiveWidget != newActiveWidget) {
            mActiveWidget = newActiveWidget;
            refresh();
        }
    }

    @Override
    public void onWeatherChanged(WeatherInfo info) {
        mWeatherInfo = info;
        mWeatherWidgetHourForecast.onWeatherChanged(mWeatherInfo);
        mWeatherWidgetDayForecast.onWeatherChanged(mWeatherInfo);
        refresh();
    }

    public void refreshTime() {
        mDateView.setDatePattern(Patterns.dateViewSkel);

        mClockView.setFormat12Hour(Patterns.clockView12);
        mClockView.setFormat24Hour(Patterns.clockView24);
    }

    private void refresh() {
        boolean useTimeDateForDateView = mActiveWidget != KeyguardWidgetScroller.TIME_WIDGET;
        Patterns.update(mContext, refreshAlarmAndWeatherStatus(), useTimeDateForDateView);
        refreshTime();
    }

    boolean refreshAlarmAndWeatherStatus() {
        AlarmManager.AlarmClockInfo nextAlarm =
                mAlarmManager.getNextAlarmClock(UserHandle.USER_CURRENT);
        String alarm = null;
        String weather = null;

        if (mWeatherInfo != null) {
            if (mWeatherInfo.formattedTemperature != null && mWeatherInfo.condition != null) {
                boolean timeWidgetVisible =
                        mActiveWidget == KeyguardWidgetScroller.TIME_WIDGET;
                boolean weatherHiddenByAlarm = !LockscreenHelper.showWeather(mContext, nextAlarm != null);
                if (timeWidgetVisible && !weatherHiddenByAlarm) {
                    final boolean showLocation = LockscreenHelper.showWeatherLocation(mContext);
                    weather = showLocation ? (mWeatherInfo.city + ", ") : "";
                    weather += mWeatherInfo.formattedTemperature + " - ";
                    weather += mWeatherInfo.condition;
                }
            }
        }
        if (nextAlarm != null && weather == null) {
            alarm = formatNextAlarm(mContext, nextAlarm);
        }
        if (weather != null) {
            mWeatherStatusView.setText(weather);
            mWeatherStatusView.setVisibility(View.VISIBLE);
        } else {
            mWeatherStatusView.setVisibility(View.GONE);
        }
        if (alarm != null) {
            mAlarmStatusView.setText(alarm);
            mAlarmStatusView.setContentDescription(
                    getResources().getString(R.string.keyguard_accessibility_next_alarm, alarm));
            mAlarmStatusView.setVisibility(View.VISIBLE);
        } else {
            mAlarmStatusView.setVisibility(View.GONE);
        }
        return weather != null || alarm != null;
    }

    public int getClockBottom() {
        return mKeyguardStatusArea.getBottom();
    }

    public float getClockTextSize() {
        return mClockView.getTextSize();
    }

    public static String formatNextAlarm(Context context, AlarmManager.AlarmClockInfo info) {
        if (info == null) {
            return "";
        }
        String skeleton = DateFormat.is24HourFormat(context, ActivityManager.getCurrentUser())
                ? "EHm"
                : "Ehma";
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton);
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    private void updateOwnerInfo() {
        if (mOwnerInfo == null) return;
        String ownerInfo = getOwnerInfo();
        if (!TextUtils.isEmpty(ownerInfo)) {
            mOwnerInfo.setVisibility(View.VISIBLE);
            mOwnerInfo.setText(ownerInfo);
        } else {
            mOwnerInfo.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mInfoCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(mContext).removeCallback(mInfoCallback);
    }

    private String getOwnerInfo() {
        String info = null;
        if (mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
            // Use the device owner information set by device policy client via
            // device policy manager.
            info = mLockPatternUtils.getDeviceOwnerInfo();
        } else {
            // Use the current user owner information if enabled.
            final boolean ownerInfoEnabled = mLockPatternUtils.isOwnerInfoEnabled(
                    KeyguardUpdateMonitor.getCurrentUser());
            if (ownerInfoEnabled) {
                info = mLockPatternUtils.getOwnerInfo(KeyguardUpdateMonitor.getCurrentUser());
            }
        }
        return info;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    // DateFormat.getBestDateTimePattern is extremely expensive, and refresh is called often.
    // This is an optimization to ensure we only recompute the patterns when the inputs change.
    private static final class Patterns {
        static String dateViewSkel;
        static String clockView12;
        static String clockView24;
        static String cacheKey;

        static void update(Context context, boolean hasWeatherOrAlarm, boolean useTimeDateForDateView) {
            final Locale locale = Locale.getDefault();
            final Resources res = context.getResources();
            if (useTimeDateForDateView) {
                dateViewSkel = res.getString(hasWeatherOrAlarm
                        ? R.string.abbrev_wday_month_day_no_year_time_alarm
                        : R.string.abbrev_wday_month_day_no_year_time);
            } else {
                dateViewSkel = res.getString(hasWeatherOrAlarm
                        ? R.string.abbrev_wday_month_day_no_year_alarm
                        : R.string.abbrev_wday_month_day_no_year);
            }
            final String clockView12Skel = res.getString(R.string.clock_12hr_format);
            final String clockView24Skel = res.getString(R.string.clock_24hr_format);
            final String key = locale.toString() + dateViewSkel + clockView12Skel + clockView24Skel;
            if (key.equals(cacheKey)) return;

            clockView12 = DateFormat.getBestDateTimePattern(locale, clockView12Skel);

            if(!context.getResources().getBoolean(com.android.systemui.R.bool.config_showAmpm)) {
                // CLDR insists on adding an AM/PM indicator even though it wasn't in the skeleton
                // format.  The following code removes the AM/PM indicator if we didn't want it.
                if (!clockView12Skel.contains("a")) {
                    clockView12 = clockView12.replaceAll("a", "").trim();
                }
            }

            clockView24 = DateFormat.getBestDateTimePattern(locale, clockView24Skel);

            // Use fancy colon.
            clockView24 = clockView24.replace(':', '\uee01');
            clockView12 = clockView12.replace(':', '\uee01');

            cacheKey = key;
        }
    }

    public void setDark(float darkAmount) {
        if (mDarkAmount == darkAmount) {
            return;
        }
        mDarkAmount = darkAmount;

        boolean dark = darkAmount == 1;
        final int N = mClockContainer.getChildCount();
        for (int i = 0; i < N; i++) {
            View child = mClockContainer.getChildAt(i);
            if (ArrayUtils.contains(mVisibleInDoze, child)) {
                continue;
            }
            child.setAlpha(dark ? 0 : 1);
        }
        if (mOwnerInfo != null) {
            mOwnerInfo.setAlpha(dark ? 0 : 1);
        }

        updateDozeVisibleViews();

        final int textColorPrimaryDark = LockScreenColorHelper.getPrimaryTextColorDark(mContext);
        final int textColorSecondaryDark = LockScreenColorHelper.getSecondaryTextColorDark(mContext);
        final int iconColorDark = LockScreenColorHelper.getNormalIconColorDark(mContext);
        final int blendedTextColorPrimary =
                ColorUtils.blendARGB(mTextColorPrimary, textColorPrimaryDark, darkAmount);
        final int blendedTextColorSecondary =
                ColorUtils.blendARGB(mTextColorSecondary, textColorSecondaryDark, darkAmount);
        final int blendedIconColor =
                ColorUtils.blendARGB(mIconColor, iconColorDark, darkAmount);

        mClockView.setTextColor(blendedTextColorPrimary);
        mWeatherWidgetHourForecast.setColors(blendedTextColorPrimary, blendedIconColor,
                blendedTextColorSecondary);
        mWeatherWidgetDayForecast.setColors(blendedTextColorPrimary, blendedIconColor,
                blendedTextColorSecondary);
        mDateView.setTextColor(blendedTextColorPrimary);
        mAlarmStatusView.setTextColor(blendedTextColorSecondary);
        mAlarmStatusView.setCompoundDrawableTintList(ColorStateList.valueOf(blendedTextColorSecondary));
        mWeatherStatusView.setTextColor(blendedTextColorSecondary);
    }

    public void setWeatherServiceController(WeatherServiceController weatherServiceController) {
        mWeatherServiceController = weatherServiceController;
    }

    private void addOnWeatherChangedCallback() {
        if (mWeatherServiceController != null) {
            mWeatherServiceController.addCallback(this);
        }
    }

    private void removeOnWeatherChangedCallback() {
        if (mWeatherServiceController != null) {
            mWeatherServiceController.removeCallback(this);
        }
    }

    private void updateColors() {
        mTextColorPrimary = mLockDarkText ? LockScreenColorHelper.getPrimaryTextColorLight(mContext)
                : LockScreenColorHelper.getPrimaryTextColorDark(mContext);
        mTextColorSecondary = mLockDarkText ? LockScreenColorHelper.getSecondaryTextColorLight(mContext)
                : LockScreenColorHelper.getSecondaryTextColorDark(mContext);
        mIconColor = mLockDarkText ? LockScreenColorHelper.getNormalIconColorLight(mContext)
                : LockScreenColorHelper.getNormalIconColorDark(mContext);
        mAlarmStatusView.setTextColor(mTextColorSecondary);
        mAlarmStatusView.setCompoundDrawableTintList(ColorStateList.valueOf(mTextColorSecondary));
        mWeatherStatusView.setTextColor(mTextColorSecondary);
        mDateView.setTextColor(mTextColorPrimary);
        mClockView.setTextColor(mTextColorPrimary);
        mWeatherWidgetHourForecast.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
        mWeatherWidgetDayForecast.setColors(mTextColorPrimary, mIconColor, mTextColorSecondary);
        mOwnerInfo.setTextColor(mTextColorSecondary);
    }

    public void setPulsing(boolean pulsing) {
        mPulsing = pulsing;
        updateDozeVisibleViews();
    }

    private void updateDozeVisibleViews() {
        for (View child : mVisibleInDoze) {
            child.setAlpha(mDarkAmount == 1 && mPulsing ? 0.8f : 1);
        }
    }
}
