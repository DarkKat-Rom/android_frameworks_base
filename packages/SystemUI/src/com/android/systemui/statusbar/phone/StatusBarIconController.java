/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.util.darkkat.ColorHelper;
import com.android.internal.util.darkkat.StatusBarColorHelper;
import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterTextView;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.darkkat.BatteryBar;
import com.android.systemui.darkkat.statusbar.NetworkTraffic;
import com.android.systemui.darkkat.statusbar.StatusBarWeather;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Controls everything regarding the icons in the status bar and on Keyguard, including, but not
 * limited to: notification icons, signal cluster, additional status icons, and clock in the status
 * bar.
 */
public class StatusBarIconController extends StatusBarIconList implements Tunable {

    public static final long DEFAULT_TINT_ANIMATION_DURATION = 120;
    public static final String ICON_BLACKLIST = "icon_blacklist";

    private static final int CLOCK_STYLE_DEFAULT  = 0;
    private static final int CLOCK_STYLE_CENTERED = 1;
    private static final int CLOCK_STYLE_HIDDEN   = 2;

    private static final int BATTERY_METER_TYPE_VERTICAL =
            BatteryMeterView.VERTICAL;
    private static final int BATTERY_METER_TYPE_HORIZONTAL_LEFT =
            BatteryMeterView.HORIZONTAL_LEFT;
    private static final int BATTERY_METER_TYPE_HORIZONTAL_RIGHT =
            BatteryMeterView.HORIZONTAL_RIGHT;
    private static final int BATTERY_METER_TYPE_CIRCLE =
            BatteryMeterView.CIRCLE;
    private static final int BATTERY_METER_TYPE_ARCS =
            BatteryMeterView.ARCS;
    private static final int BATTERY_METER_TYPE_TEXT_ONLY =
            BatteryMeterView.TEXT_ONLY;

    private Context mContext;
    private View mStatusBar;
    private PhoneStatusBar mPhoneStatusBar;
    private DemoStatusIcons mDemoStatusIcons;

    private LinearLayout mStatusBarContents;
    private CarrierText mCarrierTextKeyguard;
    private StatusBarWeather mWeatherLayout;
    private NetworkTraffic mNetworkTraffic;
    private NetworkTraffic mNetworkTrafficKeyguard;
    private LinearLayout mSystemIconArea;
    private LinearLayout mStatusIcons;
    private LinearLayout mStatusIconsKeyguard;
    private SignalClusterView mSignalCluster;
    private SignalClusterView mSignalClusterKeyguard;
    private BatteryMeterView mBatteryMeterView;
    private BatteryMeterView mBatteryMeterViewKeyguard;
    private BatteryMeterTextView mBatteryMeterTextView;
    private BatteryMeterTextView mBatteryMeterTextViewKeyguard;
    private Clock mClockDefault;
    private Clock mClockCentered;
    private LinearLayout mCenterClockLayout;
    private BatteryBar mBatteryBar;
    private BatteryBar mBatteryBarKeyguard;
    private View mTickerLayout;
    private Ticker mTicker;

    private NotificationIconAreaController mNotificationIconAreaController;
    private View mNotificationIconAreaInner;

    private int mIconSize;
    private int mIconHPadding;

    private float mDarkIntensity;
    private int mTextColor;
    private int mIconColor;
    private int mBatteryTextColor;
    private final Rect mTintArea = new Rect();
    private static final Rect sTmpRect = new Rect();
    private static final int[] sTmpInt2 = new int[2];

    private boolean mAnimateTextColor = false;
    private boolean mAnimateIconColor = false;
    private boolean mAnimateBatteryTextColor = false;

    private int mClockStyle;
    private int mBatteryMeterType = BATTERY_METER_TYPE_VERTICAL;
    private boolean mShowBatteryIconOrCircle = mBatteryMeterType < BATTERY_METER_TYPE_TEXT_ONLY;
    private boolean mShowBatteryTextOnly = mBatteryMeterType == BATTERY_METER_TYPE_TEXT_ONLY;
    private boolean mShowBatteryBar = false;

    private boolean mTransitionPending;
    private boolean mTintChangePending;
    private float mPendingDarkIntensity;
    private ValueAnimator mTintAnimator;
    private Animator mColorTransitionAnimator;

    private final Handler mHandler;
    private boolean mTransitionDeferring;
    private long mTransitionDeferringStartTime;
    private long mTransitionDeferringDuration;

    private boolean mShowTicker = false;
    private boolean mTicking;

    private final ArraySet<String> mIconBlacklist = new ArraySet<>();

    private final Runnable mTransitionDeferringDoneRunnable = new Runnable() {
        @Override
        public void run() {
            mTransitionDeferring = false;
        }
    };

    public StatusBarIconController(Context context, View statusBar, View keyguardStatusBar,
            PhoneStatusBar phoneStatusBar) {
        super(context.getResources().getStringArray(
                com.android.internal.R.array.config_statusBarIcons));
        mContext = context;
        mStatusBar = statusBar;
        mPhoneStatusBar = phoneStatusBar;
        mStatusBarContents = (LinearLayout) statusBar.findViewById(R.id.status_bar_contents);
        mCarrierTextKeyguard = (CarrierText) keyguardStatusBar.findViewById(R.id.keyguard_carrier_text);
        mWeatherLayout = (StatusBarWeather) statusBar.findViewById(R.id.status_bar_weather_layout);
        mNetworkTraffic = (NetworkTraffic) statusBar.findViewById(R.id.network_traffic);
        mNetworkTrafficKeyguard = (NetworkTraffic) keyguardStatusBar.findViewById(
                R.id.keyguard_network_traffic);
        mSystemIconArea = (LinearLayout) statusBar.findViewById(R.id.system_icon_area);
        mStatusIcons = (LinearLayout) statusBar.findViewById(R.id.statusIcons);
        mStatusIconsKeyguard = (LinearLayout) keyguardStatusBar.findViewById(R.id.statusIcons);
        mSignalCluster = (SignalClusterView) statusBar.findViewById(R.id.signal_cluster);
        mSignalClusterKeyguard = (SignalClusterView) keyguardStatusBar.findViewById(R.id.signal_cluster);
        mBatteryMeterView = (BatteryMeterView) statusBar.findViewById(R.id.battery);
        mBatteryMeterViewKeyguard = (BatteryMeterView) keyguardStatusBar.findViewById(R.id.battery);
        mBatteryMeterTextView = (BatteryMeterTextView) statusBar.findViewById(R.id.battery_meter_text);
        mBatteryMeterTextViewKeyguard =
                (BatteryMeterTextView) keyguardStatusBar.findViewById(R.id.battery_meter_text);
        mClockDefault = (Clock) statusBar.findViewById(R.id.clock);
        mClockCentered = (Clock) statusBar.findViewById(R.id.center_clock);
        mCenterClockLayout = (LinearLayout) statusBar.findViewById(R.id.center_clock_layout);
        mBatteryBar = (BatteryBar) statusBar.findViewById(R.id.battery_bar);
        mBatteryBarKeyguard = (BatteryBar) keyguardStatusBar.findViewById(R.id.battery_bar);
        mNotificationIconAreaController = SystemUIFactory.getInstance()
                .createNotificationIconAreaController(context, phoneStatusBar, this);
        mNotificationIconAreaInner =
                mNotificationIconAreaController.getNotificationInnerAreaView();
        ViewGroup notificationIconArea =
                (ViewGroup) statusBar.findViewById(R.id.notification_icon_area);

        mSignalCluster.setIconController(this);
        mSignalClusterKeyguard.setIconController(this);
        scaleBatteryMeterViews(context);
        notificationIconArea.addView(mNotificationIconAreaInner);

        mTextColor = StatusBarColorHelper.getTextColor(mContext);
        mIconColor = StatusBarColorHelper.getIconColor(mContext);
        mBatteryTextColor = StatusBarColorHelper.getBatteryTextColor(mContext);

        mHandler = new Handler();
        loadDimens();

        TunerService.get(mContext).addTunable(this, ICON_BLACKLIST);
        mColorTransitionAnimator = createColorTransitionAnimator(0, 1);
    }

    public void setSignalCluster(SignalClusterView signalCluster) {
        mSignalCluster = signalCluster;
        mSignalCluster.setIconController(this);
        mSignalCluster.setIconTint(mIconColor, 0, mDarkIntensity, mTintArea);
    }

    public void setSignalClusterKeyguard(SignalClusterView signalCluster) {
        mSignalClusterKeyguard = signalCluster;
        mSignalClusterKeyguard.setIconController(this);
        mSignalClusterKeyguard.setIconTint(mIconColor, 0, mDarkIntensity, new Rect());
    }

    /**
     * Looks up the scale factor for status bar icons and scales the battery view by that amount.
     */
    private void scaleBatteryMeterViews(Context context) {
        Resources res = context.getResources();
        TypedValue typedValue = new TypedValue();

        res.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        float iconScaleFactor = typedValue.getFloat();
        int batteryHeight = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        int batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_width);
        int marginStart = 0;
        int marginBottom = 0;
        switch (mBatteryMeterType) {
            case BATTERY_METER_TYPE_VERTICAL:
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            case BATTERY_METER_TYPE_HORIZONTAL_LEFT:
            case BATTERY_METER_TYPE_HORIZONTAL_RIGHT:
            case BATTERY_METER_TYPE_CIRCLE:
            case BATTERY_METER_TYPE_TEXT_ONLY:
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            case BATTERY_METER_TYPE_ARCS:
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_arcs_size);
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_arcs_size);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            default:
                // Do nothing
                break;
        }

        LinearLayout.LayoutParams scaledLayoutParams = new LinearLayout.LayoutParams(
                (int) (batteryWidth * iconScaleFactor), (int) (batteryHeight * iconScaleFactor));
        scaledLayoutParams.setMarginsRelative(marginStart, 0, 0, marginBottom);
        mBatteryMeterView.setLayoutParams(scaledLayoutParams);
        mBatteryMeterViewKeyguard.setLayoutParams(scaledLayoutParams);

        MarginLayoutParams lp = (MarginLayoutParams) mBatteryMeterTextView.getLayoutParams();
        lp.setMarginStart(marginStart);
        mBatteryMeterTextView.setLayoutParams(lp);
        mBatteryMeterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimensionPixelSize(R.dimen.status_bar_clock_size));
    }

    @Override
    public void onTuningChanged(String key, String newValue) {
        if (!ICON_BLACKLIST.equals(key)) {
            return;
        }
        mIconBlacklist.clear();
        mIconBlacklist.addAll(getIconBlacklist(newValue));
        ArrayList<StatusBarIconView> views = new ArrayList<StatusBarIconView>();
        // Get all the current views.
        for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
            views.add((StatusBarIconView) mStatusIcons.getChildAt(i));
        }
        // Remove all the icons.
        for (int i = views.size() - 1; i >= 0; i--) {
            removeIcon(views.get(i).getSlot());
        }
        // Add them all back
        for (int i = 0; i < views.size(); i++) {
            setIcon(views.get(i).getSlot(), views.get(i).getStatusBarIcon());
        }
    }
    private void loadDimens() {
        mIconSize = mContext.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_icon_size);
        mIconHPadding = mContext.getResources().getDimensionPixelSize(
                R.dimen.status_bar_icon_padding);
    }

    private void addSystemIcon(int index, StatusBarIcon icon) {
        String slot = getSlot(index);
        int viewIndex = getViewIndex(index);
        boolean blocked = mIconBlacklist.contains(slot);
        StatusBarIconView view = new StatusBarIconView(mContext, slot, null, blocked);
        view.set(icon);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize);
        lp.setMargins(mIconHPadding, 0, mIconHPadding, 0);
        mStatusIcons.addView(view, viewIndex, lp);

        view = new StatusBarIconView(mContext, slot, null, blocked);
        view.set(icon);
        mStatusIconsKeyguard.addView(view, viewIndex, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize));
        applyIconTint();
    }

    public void setIcon(String slot, int resourceId, CharSequence contentDescription) {
        int index = getSlotIndex(slot);
        StatusBarIcon icon = getIcon(index);
        if (icon == null) {
            icon = new StatusBarIcon(UserHandle.SYSTEM, mContext.getPackageName(),
                    Icon.createWithResource(mContext, resourceId), 0, 0, contentDescription);
            setIcon(slot, icon);
        } else {
            icon.icon = Icon.createWithResource(mContext, resourceId);
            icon.contentDescription = contentDescription;
            handleSet(index, icon);
        }
    }

    public void setExternalIcon(String slot) {
        int viewIndex = getViewIndex(getSlotIndex(slot));
        int height = mContext.getResources().getDimensionPixelSize(
                R.dimen.status_bar_icon_drawing_size);
        ImageView imageView = (ImageView) mStatusIcons.getChildAt(viewIndex);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
        imageView = (ImageView) mStatusIconsKeyguard.getChildAt(viewIndex);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
    }

    private void setHeightAndCenter(ImageView imageView, int height) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = height;
        if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).gravity = Gravity.CENTER_VERTICAL;
        }
        imageView.setLayoutParams(params);
    }

    public void setIcon(String slot, StatusBarIcon icon) {
        setIcon(getSlotIndex(slot), icon);
    }

    public void removeIcon(String slot) {
        int index = getSlotIndex(slot);
        removeIcon(index);
    }

    public void setIconVisibility(String slot, boolean visibility) {
        int index = getSlotIndex(slot);
        StatusBarIcon icon = getIcon(index);
        if (icon == null || icon.visible == visibility) {
            return;
        }
        icon.visible = visibility;
        handleSet(index, icon);
    }

    @Override
    public void removeIcon(int index) {
        if (getIcon(index) == null) {
            return;
        }
        super.removeIcon(index);
        int viewIndex = getViewIndex(index);
        mStatusIcons.removeViewAt(viewIndex);
        mStatusIconsKeyguard.removeViewAt(viewIndex);
    }

    @Override
    public void setIcon(int index, StatusBarIcon icon) {
        if (icon == null) {
            removeIcon(index);
            return;
        }
        boolean isNew = getIcon(index) == null;
        super.setIcon(index, icon);
        if (isNew) {
            addSystemIcon(index, icon);
        } else {
            handleSet(index, icon);
        }
    }

    private void handleSet(int index, StatusBarIcon icon) {
        int viewIndex = getViewIndex(index);
        StatusBarIconView view = (StatusBarIconView) mStatusIcons.getChildAt(viewIndex);
        view.set(icon);
        view = (StatusBarIconView) mStatusIconsKeyguard.getChildAt(viewIndex);
        view.set(icon);
        applyIconTint();
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        mNotificationIconAreaController.updateNotificationIcons(notificationData);
    }

    public void hideSystemIconArea(boolean animate) {
        animateHide(mSystemIconArea, animate);
        if (mClockStyle == CLOCK_STYLE_CENTERED) {
            animateHide(mCenterClockLayout, animate);
        }
        if (mShowBatteryIconOrCircle) {
            animateHide(mBatteryMeterView, animate);
        }
        if (mShowBatteryTextOnly) {
            animateHide(mBatteryMeterTextView, animate);
        }
        if (mShowBatteryBar) {
            animateHide(mBatteryBar, animate);
        }
    }

    public void showSystemIconArea(boolean animate) {
        animateShow(mSystemIconArea, animate);
        if (mClockStyle == CLOCK_STYLE_CENTERED) {
            animateShow(mCenterClockLayout, animate);
        }
        if (mShowBatteryIconOrCircle) {
            animateShow(mBatteryMeterView, animate);
        }
        if (mShowBatteryTextOnly) {
            animateShow(mBatteryMeterTextView, animate);
        }
        if (mShowBatteryBar) {
            animateShow(mBatteryBar, animate);
        }
    }

    public void hideNotificationIconArea(boolean animate) {
        animateHide(mNotificationIconAreaInner, animate);
        if (mWeatherLayout.shouldShow()) {
            animateHide(mWeatherLayout, animate);
        }
    }

    public void showNotificationIconArea(boolean animate) {
        animateShow(mNotificationIconAreaInner, animate);
        if (mWeatherLayout.shouldShow()) {
            animateShow(mWeatherLayout, animate);
        }
    }

    public void setClockVisibility(boolean visible) {
        if (mClockStyle == CLOCK_STYLE_DEFAULT) {
            mClockDefault.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (mClockStyle == CLOCK_STYLE_CENTERED) {
            mClockCentered.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void dump(PrintWriter pw) {
        int N = mStatusIcons.getChildCount();
        pw.println("  icon views: " + N);
        for (int i=0; i<N; i++) {
            StatusBarIconView ic = (StatusBarIconView) mStatusIcons.getChildAt(i);
            pw.println("    [" + i + "] icon=" + ic);
        }
        super.dump(pw);
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        if (mDemoStatusIcons == null) {
            mDemoStatusIcons = new DemoStatusIcons(mStatusIcons, mIconSize);
        }
        mDemoStatusIcons.dispatchDemoCommand(command, args);
    }

    public void dispatchClockDemoCommand(String command, Bundle args) {
        if (mClockStyle == CLOCK_STYLE_DEFAULT) {
            mClockDefault.dispatchDemoCommand(command, args);
        }
        if (mClockStyle == CLOCK_STYLE_CENTERED) {
            mClockCentered.dispatchDemoCommand(command, args);
        }
    }

    /**
     * Hides a view.
     */
    private void animateHide(final View v, boolean animate) {
        v.animate().cancel();
        if (!animate) {
            v.setAlpha(0f);
            v.setVisibility(View.INVISIBLE);
            return;
        }
        v.animate()
                .alpha(0f)
                .setDuration(160)
                .setStartDelay(0)
                .setInterpolator(Interpolators.ALPHA_OUT)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v.setVisibility(View.INVISIBLE);
                    }
                });
    }

    /**
     * Shows a view, and synchronizes the animation with Keyguard exit animations, if applicable.
     */
    private void animateShow(View v, boolean animate) {
        v.animate().cancel();
        v.setVisibility(View.VISIBLE);
        if (!animate) {
            v.setAlpha(1f);
            return;
        }
        v.animate()
                .alpha(1f)
                .setDuration(320)
                .setInterpolator(Interpolators.ALPHA_IN)
                .setStartDelay(50)

                // We need to clean up any pending end action from animateHide if we call
                // both hide and show in the same frame before the animation actually gets started.
                // cancel() doesn't really remove the end action.
                .withEndAction(null);

        // Synchronize the motion with the Keyguard fading if necessary.
        if (mPhoneStatusBar.isKeyguardFadingAway()) {
            v.animate()
                    .setDuration(mPhoneStatusBar.getKeyguardFadingAwayDuration())
                    .setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN)
                    .setStartDelay(mPhoneStatusBar.getKeyguardFadingAwayDelay())
                    .start();
        }
    }

    /**
     * Sets the dark area so {@link #setIconsDark} only affects the icons in the specified area.
     *
     * @param darkArea the area in which icons should change it's tint, in logical screen
     *                 coordinates
     */
    public void setIconsDarkArea(Rect darkArea) {
        if (darkArea == null && mTintArea.isEmpty()) {
            return;
        }
        if (darkArea == null) {
            mTintArea.setEmpty();
        } else {
            mTintArea.set(darkArea);
        }
        applyIconTint();
        mNotificationIconAreaController.setTintArea(darkArea);
    }

    public void setIconsDark(boolean dark, boolean animate) {
        if (!animate) {
            setIconTintInternal(dark ? 1.0f : 0.0f);
        } else if (mTransitionPending) {
            deferIconTintChange(dark ? 1.0f : 0.0f);
        } else if (mTransitionDeferring) {
            animateIconTint(dark ? 1.0f : 0.0f,
                    Math.max(0, mTransitionDeferringStartTime - SystemClock.uptimeMillis()),
                    mTransitionDeferringDuration);
        } else {
            animateIconTint(dark ? 1.0f : 0.0f, 0 /* delay */, DEFAULT_TINT_ANIMATION_DURATION);
        }
    }

    private void animateIconTint(float targetDarkIntensity, long delay,
            long duration) {
        if (mTintAnimator != null) {
            mTintAnimator.cancel();
        }
        if (mDarkIntensity == targetDarkIntensity) {
            return;
        }
        mTintAnimator = ValueAnimator.ofFloat(mDarkIntensity, targetDarkIntensity);
        mTintAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setIconTintInternal((Float) animation.getAnimatedValue());
            }
        });
        mTintAnimator.setDuration(duration);
        mTintAnimator.setStartDelay(delay);
        mTintAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        mTintAnimator.start();
    }

    private void setIconTintInternal(float darkIntensity) {
        mDarkIntensity = darkIntensity;
        mTextColor = (int) ArgbEvaluator.getInstance().evaluate(darkIntensity,
                StatusBarColorHelper.getTextColor(mContext),
                StatusBarColorHelper.getTextColorDarkMode(mContext));
        mIconColor = (int) ArgbEvaluator.getInstance().evaluate(darkIntensity,
                StatusBarColorHelper.getIconColor(mContext),
                StatusBarColorHelper.getIconColorDarkMode(mContext));
        mBatteryTextColor = (int) ArgbEvaluator.getInstance().evaluate(darkIntensity,
                StatusBarColorHelper.getBatteryTextColor(mContext),
                StatusBarColorHelper.getBatteryTextColorDarkMode(mContext));

        mNotificationIconAreaController.setIconTint(mIconColor);
        applyIconTint();
    }

    private void deferIconTintChange(float darkIntensity) {
        if (mTintChangePending && darkIntensity == mPendingDarkIntensity) {
            return;
        }
        mTintChangePending = true;
        mPendingDarkIntensity = darkIntensity;
    }

    /**
     * @return the tint to apply to {@param view} depending on the desired tint {@param color} and
     *         the screen {@param tintArea} in which to apply that tint
     */
    public int getTint(Rect tintArea, View view, int color) {
        if (isInArea(tintArea, view) || mDarkIntensity == 0f) {
            return color;
        } else {
            return StatusBarColorHelper.getIconColor(mContext);
        }
    }

    private int getTextTint(Rect tintArea, View view, int color) {
        if (isInArea(tintArea, view) || mDarkIntensity == 0f) {
            return color;
        } else {
            return StatusBarColorHelper.getTextColor(mContext);
        }
    }

    /**
     * @return the dark intensity to apply to {@param view} depending on the desired dark
     *         {@param intensity} and the screen {@param tintArea} in which to apply that intensity
     */
    public static float getDarkIntensity(Rect tintArea, View view, float intensity) {
        if (isInArea(tintArea, view)) {
            return intensity;
        } else {
            return 0f;
        }
    }

    /**
     * @return true if more than half of the {@param view} area are in {@param area}, false
     *         otherwise
     */
    private static boolean isInArea(Rect area, View view) {
        if (area.isEmpty() || view == null) {
            return true;
        }
        sTmpRect.set(area);
        view.getLocationOnScreen(sTmpInt2);
        int left = sTmpInt2[0];

        int intersectStart = Math.max(left, area.left);
        int intersectEnd = Math.min(left + view.getWidth(), area.right);
        int intersectAmount = Math.max(0, intersectEnd - intersectStart);

        boolean coversFullStatusBar = area.top <= 0;
        boolean majorityOfWidth = 2 * intersectAmount > view.getWidth();
        return majorityOfWidth && coversFullStatusBar;
    }

    private void applyIconTint() {
        mWeatherLayout.setTextColor(getTextTint(mTintArea, mWeatherLayout, mTextColor));
        mWeatherLayout.setIconColor(getTint(mTintArea, mWeatherLayout, mIconColor));
        mNetworkTraffic.setTextColor(getTextTint(mTintArea, mNetworkTraffic, mTextColor));
        mNetworkTraffic.setIconColor(getTint(mTintArea, mNetworkTraffic, mIconColor));
        for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mStatusIcons.getChildAt(i);
            v.setImageTintList(ColorStateList.valueOf(getTint(mTintArea, v, mIconColor)));
        }
        applyStatusIconKeyguardTint();
        mSignalCluster.setIconTint(mIconColor, StatusBarColorHelper.getIconColorDarkMode(mContext),
                mDarkIntensity, mTintArea);
        mBatteryMeterView.updateDarkIntensity(
                isInArea(mTintArea, mBatteryMeterView) ? mDarkIntensity : 0,
                StatusBarColorHelper.getIconColor(mContext),
                isInArea(mTintArea, mBatteryMeterView)
                        ? StatusBarColorHelper.getIconColorDarkMode(mContext)
                        : StatusBarColorHelper.getIconColor(mContext),
                StatusBarColorHelper.getBatteryTextColor(mContext),
                isInArea(mTintArea, mBatteryMeterView)
                        ? StatusBarColorHelper.getBatteryTextColorDarkMode(mContext)
                        : StatusBarColorHelper.getBatteryTextColor(mContext));
        mBatteryMeterTextView.setDarkIntensity(
                isInArea(mTintArea, mBatteryMeterTextView) ? mDarkIntensity : 0,
                StatusBarColorHelper.getTextColor(mContext),
                isInArea(mTintArea, mBatteryMeterTextView)
                        ? StatusBarColorHelper.getTextColorDarkMode(mContext)
                        : StatusBarColorHelper.getTextColor(mContext));
        mClockDefault.setTextColor(getTextTint(mTintArea, mClockDefault, mTextColor));
        mClockCentered.setTextColor(getTextTint(mTintArea, mClockCentered, mTextColor));
        mBatteryBar.setDarkIntensity(mDarkIntensity,
                StatusBarColorHelper.getIconColor(mContext),
                StatusBarColorHelper.getIconColorDarkMode(mContext));
        if (mTicker != null && mTickerLayout != null) {
            mTicker.setTextColor(getTextTint(mTintArea, getTickerTextView(), mTextColor));
            mTicker.setIconColorTint(ColorStateList.valueOf(getTint(
                    mTintArea, getTickerIconView(), mIconColor)));
        }
    }

    public void appTransitionPending() {
        mTransitionPending = true;
    }

    public void appTransitionCancelled() {
        if (mTransitionPending && mTintChangePending) {
            mTintChangePending = false;
            animateIconTint(mPendingDarkIntensity, 0 /* delay */, DEFAULT_TINT_ANIMATION_DURATION);
        }
        mTransitionPending = false;
    }

    public void appTransitionStarting(long startTime, long duration) {
        if (mTransitionPending && mTintChangePending) {
            mTintChangePending = false;
            animateIconTint(mPendingDarkIntensity,
                    Math.max(0, startTime - SystemClock.uptimeMillis()),
                    duration);

        } else if (mTransitionPending) {

            // If we don't have a pending tint change yet, the change might come in the future until
            // startTime is reached.
            mTransitionDeferring = true;
            mTransitionDeferringStartTime = startTime;
            mTransitionDeferringDuration = duration;
            mHandler.removeCallbacks(mTransitionDeferringDoneRunnable);
            mHandler.postAtTime(mTransitionDeferringDoneRunnable, startTime);
        }
        mTransitionPending = false;
    }

    public static ArraySet<String> getIconBlacklist(String blackListStr) {
        ArraySet<String> ret = new ArraySet<String>();
        if (blackListStr == null) {
            blackListStr = "rotate,headset";
        }
        String[] blacklist = blackListStr.split(",");
        for (String slot : blacklist) {
            if (!TextUtils.isEmpty(slot)) {
                ret.add(slot);
            }
        }
        return ret;
    }

    public void onDensityOrFontScaleChanged() {
        loadDimens();
        mWeatherLayout.onDensityOrFontScaleChanged();
        mNotificationIconAreaController.onDensityOrFontScaleChanged(mContext);
        mNetworkTraffic.onDensityOrFontScaleChanged();
        mNetworkTrafficKeyguard.onDensityOrFontScaleChanged();
        updateClock();
        for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
            View child = mStatusIcons.getChildAt(i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize);
            lp.setMargins(mIconHPadding, 0, mIconHPadding, 0);
            child.setLayoutParams(lp);
        }
        for (int i = 0; i < mStatusIconsKeyguard.getChildCount(); i++) {
            View child = mStatusIconsKeyguard.getChildAt(i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, mIconSize);
            child.setLayoutParams(lp);
        }
        scaleBatteryMeterViews(mContext);
        inflateTickerLayout();
    }

    private void updateClock() {
        FontSizeUtils.updateFontSize(mClockDefault, R.dimen.status_bar_clock_size);
        FontSizeUtils.updateFontSize(mClockCentered, R.dimen.status_bar_clock_size);
        mClockDefault.setPaddingRelative(
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.status_bar_clock_starting_padding),
                0,
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.status_bar_clock_end_padding),
                0);
        mClockCentered.setPaddingRelative(
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.status_bar_clock_starting_padding),
                0,
                mContext.getResources().getDimensionPixelSize(
                        R.dimen.status_bar_clock_end_padding),
                0);
    }

    private ValueAnimator createColorTransitionAnimator(float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);

        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float position = animation.getAnimatedFraction();
                if (mAnimateTextColor) {
                    final int blended = ColorHelper.getBlendColor(mTextColor,
                            StatusBarColorHelper.getTextColor(mContext), position);
                    mWeatherLayout.setTextColor(blended);
                    mNetworkTraffic.setTextColor(blended);
                    if (mClockStyle == CLOCK_STYLE_DEFAULT) {
                        mClockDefault.setTextColor(blended);
                    }
                    if (mClockStyle == CLOCK_STYLE_CENTERED) {
                        mClockCentered.setTextColor(blended);
                    }
                    if (mShowBatteryTextOnly) {
                        mBatteryMeterTextView.setTextColor(blended);
                    }
                }
                if (mAnimateIconColor) {
                    final int blended = ColorHelper.getBlendColor(mIconColor,
                            StatusBarColorHelper.getIconColor(mContext), position);
                    mWeatherLayout.setIconColor(blended);
                    mNetworkTraffic.setIconColor(blended);
                    for (int i = 0; i < mStatusIcons.getChildCount(); i++) {
                        StatusBarIconView v = (StatusBarIconView) mStatusIcons.getChildAt(i);
                        v.setImageTintList(ColorStateList.valueOf(blended));
                    }
                    mSignalCluster.setIconTint(blended, 0, mDarkIntensity, mTintArea);
                    if (mShowBatteryIconOrCircle) {
                        mBatteryMeterView.updateIconColor(blended);
                    }
                    if (mShowBatteryBar) {
                        mBatteryBar.setIconColor(blended);
                    }
                    mNotificationIconAreaController.setIconTint(blended);
                }
                if (mAnimateBatteryTextColor) {
                    final int blended = ColorHelper.getBlendColor(mBatteryTextColor,
                            StatusBarColorHelper.getBatteryTextColor(mContext), position);
                    if (mShowBatteryIconOrCircle) {
                        mBatteryMeterView.updateTextColor(blended);
                    }
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimateTextColor) {
                    mTextColor = StatusBarColorHelper.getTextColor(mContext);
                    mAnimateTextColor = false;
                }
                if (mAnimateIconColor) {
                    mIconColor = StatusBarColorHelper.getIconColor(mContext);
                    mAnimateIconColor = false;
                }
                if (mAnimateBatteryTextColor) {
                    mBatteryTextColor = StatusBarColorHelper.getBatteryTextColor(mContext);
                    mAnimateBatteryTextColor = false;
                }
            }
        });
        return animator;
    }

    private View getTickerTextView() {
        if (mTickerLayout != null) {
            return mTickerLayout.findViewById(R.id.tickerText);
        } else {
            return null;
        }
    }

    private View getTickerIconView() {
        if (mTickerLayout != null) {
            return mTickerLayout.findViewById(R.id.tickerIcon);
        } else {
            return null;
        }
    }

    public void updateTextColor(boolean animate) {
        mAnimateTextColor = animate;
        if (!mAnimateBatteryTextColor && !mAnimateIconColor && mAnimateTextColor) {
            mColorTransitionAnimator.start();
        }
        if (mClockStyle != CLOCK_STYLE_CENTERED || !mAnimateTextColor) {
            mClockCentered.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        }
        if (mClockStyle != CLOCK_STYLE_DEFAULT || !mAnimateTextColor) {
            mClockDefault.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        }
        mCarrierTextKeyguard.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        mNetworkTrafficKeyguard.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        if (!mShowBatteryTextOnly) {
            mBatteryMeterTextView.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        }
        mBatteryMeterTextViewKeyguard.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        if (mTicker != null && mTickerLayout != null) {
            mTicker.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        }
    }

    public void updateIconColor(boolean animate) {
        mAnimateIconColor = animate;
        if (!mAnimateTextColor && !mAnimateBatteryTextColor && mAnimateIconColor) {
            mColorTransitionAnimator.start();
        }
        applyStatusIconKeyguardTint();
        mSignalClusterKeyguard.setIconTint(StatusBarColorHelper.getIconColor(mContext), 0,
                mDarkIntensity, new Rect());
        mNetworkTrafficKeyguard.setIconColor(StatusBarColorHelper.getIconColor(mContext));
        if (!mShowBatteryIconOrCircle) {
            mBatteryMeterView.updateIconColor(StatusBarColorHelper.getIconColor(mContext));
        }
        if (!mShowBatteryBar) {
            mBatteryBar.setIconColor(StatusBarColorHelper.getIconColor(mContext));
        }
        mBatteryMeterViewKeyguard.updateIconColor(StatusBarColorHelper.getIconColor(mContext));
        mBatteryBarKeyguard.setIconColor(StatusBarColorHelper.getIconColor(mContext));
        if (mTicker != null && mTickerLayout != null) {
            mTicker.setIconColorTint(ColorStateList.valueOf(StatusBarColorHelper.getIconColor(mContext)));
        }
    }

    public void updateBatteryTextColor(boolean animate) {
        mAnimateBatteryTextColor = animate;
        if (!mAnimateTextColor && !mAnimateIconColor && mAnimateBatteryTextColor) {
            mColorTransitionAnimator.start();
        }
        if (!mShowBatteryIconOrCircle) {
            mBatteryMeterView.updateTextColor(StatusBarColorHelper.getBatteryTextColor(mContext));
        }
        mBatteryMeterViewKeyguard.updateTextColor(StatusBarColorHelper.getBatteryTextColor(mContext));
    }

    private void applyStatusIconKeyguardTint() {
        for (int i = 0; i < mStatusIconsKeyguard.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) mStatusIconsKeyguard.getChildAt(i);
            v.setImageTintList(ColorStateList.valueOf(StatusBarColorHelper.getIconColor(mContext)));
        }
    }

    public void updateWeatherVisibility(boolean show, boolean forceHide, int maxAllowedIcons) {
        boolean forceHideByNumberOfIcons = false;
        int notificationIconsCount = mNotificationIconAreaController.getNotificationIconsCount();
        if (forceHide && notificationIconsCount >= maxAllowedIcons) {
            forceHideByNumberOfIcons = true;
        }
        mWeatherLayout.setShow(show && !forceHideByNumberOfIcons);
    }

    public void updateWeatherType(int type) {
        mWeatherLayout.setType(type);
    }

    public void updateShowNetworkTraffic(boolean show) {
        mNetworkTraffic.setShow(show);
    }

    public void updateShowNetworkTrafficOnKeyguard(boolean show) {
        mNetworkTrafficKeyguard.setShow(show);
    }

    public void updateNetworkTrafficActivity(int activity) {
        mNetworkTraffic.setActivity(activity);
        mNetworkTrafficKeyguard.setActivity(activity);
    }

    public void updateNetworkTrafficType(int type) {
        mNetworkTraffic.setType(type);
        mNetworkTrafficKeyguard.setType(type);
    }

    public void updateNetworkTrafficIsBit(boolean isBit) {
        mNetworkTraffic.setIsBit(isBit);
        mNetworkTrafficKeyguard.setIsBit(isBit);
    }

    public void updateNetworkTrafficHideTraffic(boolean hide, int threshold, boolean iconAsIndicator) {
        mNetworkTraffic.setHide(hide, threshold, iconAsIndicator);
        mNetworkTrafficKeyguard.setHide(hide, threshold, iconAsIndicator);
    }

    public void updateClockStyle(int clockStyle) {
        mClockStyle = clockStyle;

        switch (mClockStyle) {
            case CLOCK_STYLE_DEFAULT:
                mClockCentered.setVisibility(View.GONE);
                mCenterClockLayout.setVisibility(View.GONE);
                mClockDefault.setVisibility(View.VISIBLE);
                break;
            case CLOCK_STYLE_CENTERED:
                mClockDefault.setVisibility(View.GONE);
                mCenterClockLayout.setVisibility(View.VISIBLE);
                mClockCentered.setVisibility(View.VISIBLE);
                break;
            case CLOCK_STYLE_HIDDEN:
                mClockDefault.setVisibility(View.GONE);
                mCenterClockLayout.setVisibility(View.GONE);
                mClockCentered.setVisibility(View.GONE);
                break;
        }
        mNotificationIconAreaController.setCenteredClock(mClockStyle == CLOCK_STYLE_CENTERED);
    }

    public void updateClockSettings() {
        mClockDefault.updateSettings();
        mClockCentered.updateSettings();
    }

    public void setupBatteryMeter(int type, int iconColor, int textColor, boolean showText,
            int dotInterval, int dotLength, boolean showChargeAnimation, boolean cutOutText) {
        mBatteryMeterType = type;
        mShowBatteryIconOrCircle = mBatteryMeterType < BATTERY_METER_TYPE_TEXT_ONLY;
        mShowBatteryTextOnly = mBatteryMeterType == BATTERY_METER_TYPE_TEXT_ONLY;

        mBatteryMeterView.setupIcon(type, iconColor, textColor, showText,
                dotInterval, dotLength, showChargeAnimation, cutOutText);
        mBatteryMeterViewKeyguard.setupIcon(type, iconColor, textColor, showText,
                dotInterval, dotLength, showChargeAnimation, cutOutText);
        mBatteryMeterTextView.setup(StatusBarColorHelper.getTextColor(mContext), showChargeAnimation);
        mBatteryMeterTextViewKeyguard.setup(
                StatusBarColorHelper.getTextColor(mContext), showChargeAnimation);
        if (mShowBatteryTextOnly) {
            mBatteryMeterTextView.setVisibility(View.VISIBLE);
            mBatteryMeterTextViewKeyguard.setVisibility(View.VISIBLE);
        } else {
            mBatteryMeterTextView.setVisibility(View.GONE);
            mBatteryMeterTextViewKeyguard.setVisibility(View.GONE);
        }
    }

    public void updateBatteryBarVisibility(boolean show) {
        mShowBatteryBar = show;
        mBatteryBar.setVisibility(mShowBatteryBar ? View.VISIBLE : View.GONE);
    }

    public void updateBatteryBarVisibilityOnKeyguard(boolean show) {
        mBatteryBarKeyguard.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void updateBatteryMeterTextVisibility(boolean show) {
        mBatteryMeterView.updateTextVisibility(show);
        mBatteryMeterViewKeyguard.updateTextVisibility(show);
    }

    public void updateBatteryMeterCircleDots(int interval, int length) {
        mBatteryMeterView.updateCircleDots(interval, length);
        mBatteryMeterViewKeyguard.updateCircleDots(interval, length);
    }

    public void updateBatteryShowChargeAnimation(boolean show) {
        mBatteryMeterView.setShowChargeAnimation(show);
        mBatteryMeterViewKeyguard.setShowChargeAnimation(show);
        mBatteryMeterTextView.setShowChargeAnimation(show);
        mBatteryMeterTextViewKeyguard.setShowChargeAnimation(show);
        mBatteryBar.setShowChargeAnimation(show);
        mBatteryBarKeyguard.setShowChargeAnimation(show);
    }

    public void updateBatteryMeterCutOutText(boolean cutOut) {
        mBatteryMeterView.updateCutOutText(cutOut);
        mBatteryMeterViewKeyguard.updateCutOutText(cutOut);
    }

    public void updateShowTicker(boolean show) {
        mShowTicker = show;
        if (mShowTicker && (mTicker == null || mTickerLayout == null)) {
            inflateTickerLayout();
        }
    }

    private void inflateTickerLayout() {
        if (mTickerLayout != null) {
            if (mTicker!= null) {
                mTicker = null;
            }
            ((ViewGroup) mStatusBar).removeView(mTickerLayout);
            mTickerLayout = null;
        }
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTickerLayout = inflater.inflate(R.layout.status_bar_ticker, null);
        ((ViewGroup) mStatusBar).addView(mTickerLayout);
        mTicker = new MyTicker(mContext, mTickerLayout);

        TickerText tickerText = (TickerText) mTickerLayout.findViewById(R.id.tickerText);
        tickerText.mTicker = mTicker;
        mTicker.setTextColor(StatusBarColorHelper.getTextColor(mContext));
        mTicker.setIconColorTint(ColorStateList.valueOf(StatusBarColorHelper.getIconColor(mContext)));
    }

    public void addTickerEntry(StatusBarNotification n) {
        mTicker.addEntry(n);
    }

    public void removeTickerEntry(StatusBarNotification n) {
        mTicker.removeEntry(n);
    }

    public void haltTicker() {
        if (mTicking) {
            mTicker.halt();
        }
    }

    private class MyTicker extends Ticker {
        MyTicker(Context context, View sb) {
            super(context, sb);
        }

        @Override
        public void tickerStarting() {
            if (!mShowTicker) return;
            mTicking = true;
            mStatusBarContents.setVisibility(View.GONE);
            if (mClockStyle == CLOCK_STYLE_CENTERED) {
                mCenterClockLayout.setVisibility(View.GONE);
            }
            mTickerLayout.setVisibility(View.VISIBLE);
            mTickerLayout.startAnimation(loadAnim(com.android.internal.R.anim.push_up_in, null));
            mStatusBarContents.startAnimation(loadAnim(com.android.internal.R.anim.push_up_out, null));
            if (mClockStyle == CLOCK_STYLE_CENTERED) {
                mCenterClockLayout.startAnimation(loadAnim(com.android.internal.R.anim.push_up_out, null));
            }
        }

        @Override
        public void tickerDone() {
            if (!mShowTicker) return;

            mStatusBarContents.setVisibility(View.VISIBLE);
            if (mClockStyle == CLOCK_STYLE_CENTERED) {
                mCenterClockLayout.setVisibility(View.VISIBLE);
            }
            mTickerLayout.setVisibility(View.GONE);
            mStatusBarContents.startAnimation(loadAnim(com.android.internal.R.anim.push_down_in, null));
            if (mClockStyle == CLOCK_STYLE_CENTERED) {
                mCenterClockLayout.startAnimation(loadAnim(com.android.internal.R.anim.push_down_in, null));
            }
            mTickerLayout.startAnimation(loadAnim(com.android.internal.R.anim.push_down_out,
                        mTickingDoneListener));
        }

        public void tickerHalting() {
            if (!mShowTicker) return;

            if (mStatusBarContents.getVisibility() != View.VISIBLE) {
                mStatusBarContents.setVisibility(View.VISIBLE);
                mStatusBarContents
                        .startAnimation(loadAnim(com.android.internal.R.anim.fade_in, null));
            }
            if (mClockStyle == CLOCK_STYLE_CENTERED) {
                if (mCenterClockLayout.getVisibility() != View.VISIBLE) {
                    mCenterClockLayout.setVisibility(View.VISIBLE);
                    mStatusBarContents
                            .startAnimation(loadAnim(com.android.internal.R.anim.fade_in, null));
                }
            }

            mTickerLayout.setVisibility(View.GONE);
            // we do not animate the ticker away at this point, just get rid of it (b/6992707)

        }
    }

    Animation.AnimationListener mTickingDoneListener = new Animation.AnimationListener() {;
        public void onAnimationEnd(Animation animation) {
            mTicking = false;
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationStart(Animation animation) {
        }
    };

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }
}
