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
 * limitations under the License.
 */

package com.android.systemui;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.android.systemui.statusbar.policy.BatteryController;

import java.text.NumberFormat;

public class BatteryMeterTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback {

    private static final float CHARGING_BOLT_ASPECT_RATIO = 10f / 19f;

    private static final int LEVEL_FULL = 100;

    private final Context mContext;
    private Handler mHandler;

    private int mHeight;
    private int mWidth;
    private float mTextHeight;
    private float mWarningTextHeight;
    private int mChargingBoltTop;
    private int mChargingBoltPadding;

    private String mPercentageString;
    private final int mCriticalLevel;

    private final int[] mColors;
    private int mFrameColor;
    private int mTextColor = Color.WHITE;

    private final TextPaint mFramePaint;
    private final TextPaint mTextPaint;
    private final TextPaint mWarningTextPaint;

    private Drawable mChargingBolt;

    private final int mDarkModeBackgroundColor;
    private final int mDarkModeFillColor;
    private final int mLightModeBackgroundColor;
    private final int mLightModeFillColor;

    private int mLevel = -1;
    private boolean mPluggedIn = false;
    private boolean mCharging = false;
    private boolean mPowerSaveEnabled;

    private boolean mShowChargeAnimation = false;
    private boolean mIsAnimating = false;
    private int mAnimationLevel = 0;

    private boolean mAttached = false;
    private boolean mCallbackAdded = false;

    private BatteryController mBatteryController;

    private final Runnable mAnimateCharging = new Runnable() {
        public void run() {
            updateChargeAnim();
        }
    };

    public BatteryMeterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHandler = new Handler();
        final Resources res = mContext.getResources();

        mCriticalLevel = res.getInteger(
                com.android.internal.R.integer.config_criticalBatteryWarningLevel);

        TypedArray levels = res.obtainTypedArray(R.array.batterymeter_color_levels);
        TypedArray colors = res.obtainTypedArray(R.array.batterymeter_color_values);
        final int N = levels.length();
        mColors = new int[2*N];
        for (int i=0; i<N; i++) {
            mColors[2*i] = levels.getInt(i, 0);
            mColors[2*i+1] = colors.getColor(i, 0);
        }
        levels.recycle();
        colors.recycle();
        mFrameColor = context.getColor(R.color.batterymeter_frame_color);

        mFramePaint = new TextPaint(getPaint());
        mFramePaint.setTextAlign(Paint.Align.CENTER);
        mFramePaint.setColor(mFrameColor);

        mTextPaint = new TextPaint(getPaint());
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mWarningTextPaint = new TextPaint(getPaint());
        mWarningTextPaint.setTextAlign(Paint.Align.CENTER);
        mWarningTextPaint.setColor(mColors[1]);

        mChargingBolt = mContext.getDrawable(R.drawable.ic_charging_bolt);

        mDarkModeBackgroundColor =
                context.getColor(R.color.dark_mode_icon_color_dual_tone_background);
        mDarkModeFillColor = context.getColor(R.color.dark_mode_icon_color_dual_tone_fill);
        mLightModeBackgroundColor =
                context.getColor(R.color.light_mode_icon_color_dual_tone_background);
        mLightModeFillColor = context.getColor(R.color.light_mode_icon_color_dual_tone_fill);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mHeight = h;
        mWidth = w;
    }

    @Override
    public void setTextSize(int unit, float size) {
        float textSize = TypedValue.applyDimension(unit, size, mContext.getResources().getDisplayMetrics());
        mFramePaint.setTextSize(textSize);
        mTextPaint.setTextSize(textSize);
        super.setTextSize(unit, size);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAttached = true;
        if (mBatteryController != null && !mCallbackAdded) {
            mBatteryController.addStateChangedCallback(this);
            mCallbackAdded = true;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached = false;
        if (mBatteryController != null && mCallbackAdded) {
            mBatteryController.removeStateChangedCallback(this);
            mCallbackAdded = false;
        }
    }

    public void setBatteryController(BatteryController controller) {
        mBatteryController = controller;
        if (mAttached && !mCallbackAdded) {
            mBatteryController.addStateChangedCallback(this);
            mCallbackAdded = true;
        }
    }

    private boolean isAttachedAndVisible() {
        return getVisibility() == View.VISIBLE && mAttached;
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        mLevel = level;
        mPluggedIn = pluggedIn;
        mCharging = charging;
        String percentage = NumberFormat.getPercentInstance().format((double) mLevel / 100.0);
        String plus = mIsAnimating || !mPowerSaveEnabled ? "" : " +";
        String warning = !mIsAnimating && !mPowerSaveEnabled && mLevel <= mCriticalLevel ? " !" : "";
        mPercentageString = percentage + plus + warning;
        setText(mPercentageString);
        if (shouldDrawChargingBolt()) {
            updateChargingBoltSize();

        }
        setCompoundDrawablePadding(shouldDrawChargingBolt() ? mChargingBoltPadding : 0);
        setCompoundDrawablesRelative(null, null, shouldDrawChargingBolt() ? mChargingBolt : null, null);
        startChargeAnim();
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        mPowerSaveEnabled = isPowerSave;
        String percentage = NumberFormat.getPercentInstance().format((double) mLevel / 100.0);
        String plus = mIsAnimating || !mPowerSaveEnabled ? "" : " +";
        String warning = !mIsAnimating && !mPowerSaveEnabled && mLevel <= mCriticalLevel ? " !" : "";
        mPercentageString = percentage + plus + warning;
        setText(mPercentageString);
    }

    private int getTextColorForLevel(int percent) {

        // If we are in power save mode, always use the normal color.
        if (mPowerSaveEnabled) {
            return mTextColor;
        }
        int thresh, color = 0;
        for (int i=0; i<mColors.length; i+=2) {
            thresh = mColors[i];
            color = mColors[i+1];
            if (percent <= thresh) {

                // Respect tinting for "normal" level
                if (i == mColors.length-2) {
                    return mTextColor;
                } else {
                    return color;
                }
            }
        }
        return color;
    }

    @Override
    public void setTextColor(int textColor) {
        mFrameColor = (mFrameColor & 0xff000000) | (textColor & 0x00ffffff);
        mTextColor = textColor;
        mChargingBolt.setTintList(ColorStateList.valueOf(mTextColor));
        super.setTextColor(textColor);
    }


    public void setDarkIntensity(float darkIntensity, int textColor, int textColorDark) {
        int backgroundTint = getBackgroundColor(darkIntensity, textColor, textColorDark);
        int textTint = getTextColor(darkIntensity, textColor, textColorDark);
        mFrameColor = backgroundTint;
        mTextColor = textTint;
        super.setTextColor(textColor);
    }

    private int getBackgroundColor(float darkIntensity, int lightColor, int darkColor) {
        int lightModeColor = (mLightModeBackgroundColor & 0xff000000) | (lightColor & 0x00ffffff);
        int darkModeColor = (mDarkModeBackgroundColor & 0xff000000) | (darkColor & 0x00ffffff);
        return getColorForDarkIntensity(darkIntensity, lightModeColor, darkModeColor);
    }

    private int getTextColor(float darkIntensity, int lightColor, int darkColor) {
        int lightModeColor = (mLightModeFillColor & 0xff000000) | (lightColor & 0x00ffffff);
        int darkModeColor = (mDarkModeFillColor & 0xff000000) | (darkColor & 0x00ffffff);
        return getColorForDarkIntensity(darkIntensity, lightModeColor, darkModeColor);
    }

    private int getColorForDarkIntensity(float darkIntensity, int lightColor, int darkColor) {
        return (int) ArgbEvaluator.getInstance().evaluate(darkIntensity, lightColor, darkColor);
    }

    public void setShowChargeAnimation(boolean showChargeAnimation) {
        mShowChargeAnimation = showChargeAnimation;
        if (shouldDrawChargingBolt()) {
            updateChargingBoltSize();
        }
        setCompoundDrawablePadding(shouldDrawChargingBolt() ? mChargingBoltPadding : 0);
        setCompoundDrawablesRelative(null, null, shouldDrawChargingBolt() ? mChargingBolt : null, null);
        startChargeAnim();
    }

    public void setup(int textColor, boolean showChargeAnimation) {
        mHandler.removeCallbacks(mAnimateCharging);
        mFrameColor = (mFrameColor & 0xff000000) | (textColor & 0x00ffffff);
        mTextColor = textColor;
        mShowChargeAnimation = showChargeAnimation;
        super.setTextColor(textColor);
    }

    @Override
    public void draw(Canvas c) {
        final int level = mIsAnimating ? mAnimationLevel : mLevel;
        if (level == -1) return;

        // apply frame tint
        mFramePaint.setColor(mFrameColor);
        // apply text tint
        mTextPaint.setColor(getTextColorForLevel(mCharging || mIsAnimating ? 50 : mLevel));

        final float width = mTextPaint.measureText(mPercentageString);
        final float pctX = width * 0.5f;
        final float pctY = -mTextPaint.getFontMetrics().top;
        if (mIsAnimating) {
            c.drawText(mPercentageString, pctX, pctY, mFramePaint);

            float drawFrac = (float) level / 100f;

            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(mPercentageString, 0, mPercentageString.length(), textBounds);
            float textHeight = textBounds.height();
            float textBottom = (mHeight + textHeight) * 0.5f;
            float clipTop = textBottom - textHeight * drawFrac;
            float clipBottom = mHeight;

            RectF clipBounds = new RectF(0f, clipTop, width, clipBottom);
            c.clipRect(clipBounds, Region.Op.REPLACE);
        }
        c.drawText(mPercentageString, pctX, pctY, mTextPaint);

        if (shouldDrawChargingBolt()) {
            c.translate(width + mChargingBoltPadding, mChargingBoltTop);
            mChargingBolt.draw(c);
        }
    }

    private boolean shouldDrawChargingBolt() {
        return mPluggedIn && mCharging && !mIsAnimating;
    }

    private void startChargeAnim() {
        if (!mPluggedIn || !mCharging || !mShowChargeAnimation || !isAttachedAndVisible()
                || mIsAnimating) {
            return;
        }
        mIsAnimating = true;
        mAnimationLevel = LEVEL_FULL;
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
        // the meter animated back to full level
        if (((!mPluggedIn || !mCharging) && mAnimationLevel == LEVEL_FULL)
                || (!mShowChargeAnimation && mAnimationLevel == LEVEL_FULL)
                || !isAttachedAndVisible()) {
            mHandler.removeCallbacks(mAnimateCharging);
            mIsAnimating = false;
            mAnimationLevel = LEVEL_FULL;
            onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);
            return;
        }

        if (mAnimationLevel > LEVEL_FULL) {
            mAnimationLevel = 0;
        } else {
            mAnimationLevel += 1;
        }

        onBatteryLevelChanged(mLevel, mPluggedIn, mCharging);

        mHandler.removeCallbacks(mAnimateCharging);
        mHandler.postDelayed(mAnimateCharging, 50);
    }

    private void updateChargingBoltSize() {
        mChargingBoltPadding = mContext.getResources().getDimensionPixelSize(
                R.dimen.battery_charging_bolt_text_padding);
        Rect textBounds = new Rect();
        mTextPaint.getTextBounds(mPercentageString, 0, mPercentageString.length(), textBounds);
        int height = Math.round(textBounds.height());
        int width = Math.round(CHARGING_BOLT_ASPECT_RATIO * height);
        mChargingBoltTop = Math.round((mHeight - height) * 0.5f);
        mChargingBolt.setBounds(0, 0, width, height);
    }
}
