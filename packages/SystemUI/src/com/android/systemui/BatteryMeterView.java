/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;

import com.android.systemui.statusbar.policy.BatteryController;

public class BatteryMeterView extends ImageView implements
        BatteryController.BatteryStateChangeCallback {

    public static final int VERTICAL         = 0;
    public static final int HORIZONTAL_LEFT  = 1;
    public static final int HORIZONTAL_RIGHT = 2;
    public static final int CIRCLE           = 3;
    public static final int ARCS             = 4;
    public static final int TEXT_ONLY        = 5;
    public static final int HIDDEN           = 6;

    private static final float ROTATION_LEFT_DEGREES  = -90f;
    private static final float ROTATION_RIGHT_DEGREES = 90f;
    public static final float ROTATION_NONE           = 0f;

    private static final int LEVEL_FULL = 100;

    private final Context mContext;
    private final Handler mHandler;

    private BatteryMeterBaseDrawable mDrawable;
    private BatteryController mBatteryController;

    private int mIconColor = Color.WHITE;
    private int mTextColor = Color.WHITE;

    private int mLevel = -1;
    private int mAnimationLevel = 0;
    private boolean mPluggedIn = false;
    private boolean mCharging = false;
    private boolean mIsAnimating = false;

    private int mType = VERTICAL;
    private boolean mShowPercent = false;
    private int mDotInterval = 0;
    private int mDotLength = 0;
    private boolean mShowChargeAnimation = false;
    private boolean mCutOutText = true;

    private boolean mAttached = false;

    private final Runnable mAnimateCharging = new Runnable() {
        public void run() {
            updateChargeAnim();
        }
    };

    public BatteryMeterView(Context context) {
        this(context, null, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;
        mHandler = new Handler();

        mDrawable = new BatteryMeterDrawable(mContext, new Handler(), ROTATION_NONE,
                        ROTATION_NONE);
        setImageDrawable(mDrawable);

    }

    public void setupIcon(int type, int iconColor, int textColor, boolean showText,
            int dotInterval, int dotLength, boolean showChargeAnimation, boolean cutOutText) {
        mHandler.removeCallbacks(mAnimateCharging);
        Resources res = mContext.getResources();

        mType = type;
        int batteryHeight = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        int batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_width);
        int marginStart = 0;
        int marginBottom = 0;
        switch (mType) {
            case VERTICAL:
                mDrawable = new BatteryMeterDrawable(mContext, new Handler(), ROTATION_NONE,
                        ROTATION_NONE);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            case HORIZONTAL_LEFT:
                mDrawable = new BatteryMeterDrawable(mContext, new Handler(),
                        ROTATION_LEFT_DEGREES, ROTATION_RIGHT_DEGREES);
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            case HORIZONTAL_RIGHT:
                mDrawable = new BatteryMeterDrawable(mContext, new Handler(),
                        ROTATION_RIGHT_DEGREES, ROTATION_LEFT_DEGREES);
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            case CIRCLE:
                mDrawable = new BatteryMeterCircleDrawable(mContext, new Handler());
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            case ARCS:
                mDrawable = new BatteryMeterArcsDrawable(mContext, new Handler());
                batteryHeight = res.getDimensionPixelSize(R.dimen.status_bar_battery_arcs_size);
                batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_arcs_size);
                marginStart = res.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
                marginBottom = res.getDimensionPixelSize(R.dimen.battery_margin_bottom);
                break;
            default:
                // Do nothing
                break;
        }

        mIconColor = iconColor;
        mTextColor = textColor;
        mShowPercent = showText;
        mDotInterval = dotInterval;
        mDotLength = dotLength;
        mShowChargeAnimation = showChargeAnimation;
        mCutOutText = cutOutText;
        mIsAnimating = false;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(batteryWidth, batteryHeight);
        lp.setMarginsRelative(marginStart, 0, 0, marginBottom);
        setLayoutParams(lp);

        if (mType < TEXT_ONLY) {
            setVisibility(View.VISIBLE);
            setImageDrawable(mDrawable);
            mDrawable.setup(mIconColor, mTextColor, mShowPercent, mDotInterval, mDotLength,
                    mShowChargeAnimation, mCutOutText);
            startChargeAnim();
            if (!mIsAnimating) {
                mDrawable.onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
            }
        } else {
            setImageDrawable(null);
            setVisibility(View.GONE);
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAttached = true;
        mBatteryController.addStateChangedCallback(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached = false;
        mBatteryController.removeStateChangedCallback(this);
    }

    private boolean isAttachedAndVisible() {
        return getVisibility() == View.VISIBLE && mAttached;
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        mLevel = level;
        mPluggedIn = pluggedIn;
        mCharging = charging;
        startChargeAnim();
        if (!mIsAnimating) {
            mDrawable.onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
        }
        setContentDescription(
                getContext().getString(charging ? R.string.accessibility_battery_level_charging
                        : R.string.accessibility_battery_level, level));
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        mDrawable.onPowerSaveChanged(isPowerSave);
    }

    public void setBatteryController(BatteryController mBatteryController) {
        this.mBatteryController = mBatteryController;
    }

    public void updateIconColor(int iconColor) {
        mIconColor = iconColor;
        mDrawable.setIconColor(mIconColor);
    }

    public void updateTextColor(int textColor) {
        mTextColor = textColor;
        mDrawable.setTextColor(mTextColor);
    }

    public void updateDarkIntensity(float darkIntensity, int fillColor, int fillColorDark,
            int textColor, int textColorDark) {
        mDrawable.setDarkIntensity(darkIntensity, fillColor, fillColorDark, textColor, textColorDark);
    }

    public void updateTextVisibility(boolean show) {
        mShowPercent = show;
        mDrawable.setTextVisibility(mShowPercent);
    }

    public void updateCircleDots(int interval, int length) {
        mDotInterval = interval;
        mDotLength = length;
        mDrawable.setCircleDots(mDotInterval, mDotLength);
    }

    public void setShowChargeAnimation(boolean showChargeAnimation) {
        mShowChargeAnimation = showChargeAnimation;
        mDrawable.setShowChargeAnimation(showChargeAnimation);
        if (!mIsAnimating) {
            startChargeAnim();
        }
    }

    public void updateCutOutText(boolean cutOutText) {
        mCutOutText = cutOutText;
        mDrawable.setCutOutText(mCutOutText);
    }

    private void startChargeAnim() {
        if (!mPluggedIn || !mCharging || !mShowChargeAnimation || !isAttachedAndVisible()
                || mIsAnimating) {
            return;
        }
        mIsAnimating = true;
        mAnimationLevel = mLevel;
        updateChargeAnim();
    }

    /**
     * updates the animation counter
     * cares for timed callbacks to continue animation cycles
     * uses mInvalidate for delayed invalidate() callbacks
     */
    private void updateChargeAnim() {
        // Stop animation: 
        // when after unplugging/after disabling charge animation,
        // the meter animated back to the current level
        if (((!mPluggedIn || !mCharging) && mAnimationLevel == mLevel)
                || (!mShowChargeAnimation && mAnimationLevel == mLevel)
                || !isAttachedAndVisible()) {
            mHandler.removeCallbacks(mAnimateCharging);
            mIsAnimating = false;
            mAnimationLevel = mLevel;
            mDrawable.onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
            return;
        }

        if (mAnimationLevel > LEVEL_FULL) {
            mAnimationLevel = 0;
        } else {
            mAnimationLevel += 1;
        }

        mDrawable.onBatteryLevelChanged(mLevel, mAnimationLevel, mPluggedIn, mCharging);

        mHandler.removeCallbacks(mAnimateCharging);
        mHandler.postDelayed(mAnimateCharging, 50);
    }
}
