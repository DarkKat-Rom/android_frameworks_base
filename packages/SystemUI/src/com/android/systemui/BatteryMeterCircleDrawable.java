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
import android.annotation.Nullable;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;

public class BatteryMeterCircleDrawable extends BatteryMeterBaseDrawable {
    private static final float STROKE_WITH = 6.5f;

    private final Context mContext;
    private final Handler mHandler;

    private int mHeight;
    private int mWidth;
    private int mCircleSize;
    private float mTextHeight;
    private float mWarningTextHeight;
    private float mTextX;
    private float mTextY;
    private final int mIntrinsicWidth;
    private final int mIntrinsicHeight;

    private final String mWarningString;
    private final int mCriticalLevel;

    private final int[] mColors;
    private int mIconTint = Color.WHITE;
    private int mFrameColor;
    private int mTextColor = Color.WHITE;

    private final Paint mFramePaint;
    private final Paint mFillPaint;
    private final Paint mWarningTextPaint;
    private final Paint mTextPaint;
    private final Paint mBoltPaint;
    private final Paint mPlusPaint;

    private final float[] mBoltPoints;
    private final float[] mPlusPoints;

    private final Path mBoltPath = new Path();
    private final Path mPlusPath = new Path();

    private RectF mDrawRect;
    private final RectF mBoltFrame = new RectF();
    private final RectF mPlusFrame = new RectF();

    private final int mDarkModeBackgroundColor;
    private final int mDarkModeFillColor;
    private final int mLightModeBackgroundColor;
    private final int mLightModeFillColor;

    private int mLevel = -1;
    private int mAnimLevel = 0;
    private boolean mPluggedIn = false;
    private boolean mCharging = false;
    private boolean mIsAnimating = false;
    private boolean mPowerSaveEnabled;

    private boolean mShowPercent = false;
    private boolean mIsCircleDotted = false;
    private int mDotLength = 0;
    private int mDotInterval = 0;
    private boolean mShowChargeAnimation = false;

    public BatteryMeterCircleDrawable(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        final Resources res = mContext.getResources();

        mIntrinsicWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        mIntrinsicHeight = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);

        mWarningString = context.getString(R.string.battery_meter_very_low_overlay_symbol);
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

        mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFramePaint.setStrokeCap(Paint.Cap.BUTT);
        mFramePaint.setDither(true);
        mFramePaint.setStrokeWidth(0);
        mFramePaint.setStyle(Paint.Style.STROKE);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStrokeCap(Paint.Cap.BUTT);
        mFillPaint.setDither(true);
        mFillPaint.setStrokeWidth(0);
        mFillPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Typeface font = Typeface.create("sans-serif-condensed", Typeface.BOLD);
        mTextPaint.setTypeface(font);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mWarningTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWarningTextPaint.setColor(mColors[1]);
        font = Typeface.create("sans-serif", Typeface.BOLD);
        mWarningTextPaint.setTypeface(font);
        mWarningTextPaint.setTextAlign(Paint.Align.CENTER);

        mBoltPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoltPoints = loadBoltPoints(res);

        mPlusPaint = new Paint(mBoltPaint);
        mPlusPoints = loadPlusPoints(res);

        mDarkModeBackgroundColor =
                context.getColor(R.color.dark_mode_icon_color_dual_tone_background);
        mDarkModeFillColor = context.getColor(R.color.dark_mode_icon_color_dual_tone_fill);
        mLightModeBackgroundColor =
                context.getColor(R.color.light_mode_icon_color_dual_tone_background);
        mLightModeFillColor = context.getColor(R.color.light_mode_icon_color_dual_tone_fill);
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    private void postInvalidate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateSelf();
            }
        });
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        mLevel = level;
        SINGLE_DIGIT_PERCENT = mLevel < 10;
        mPluggedIn = pluggedIn;
        mCharging = charging;
        mIsAnimating = false;
        mAnimLevel = 0;
        postInvalidate();
    }

    @Override
    public void onBatteryLevelChanged(int level, int animLevel, boolean pluggedIn, boolean charging) {
        mLevel = level;
        mPluggedIn = pluggedIn;
        mCharging = charging;
        mIsAnimating = true;
        mAnimLevel = animLevel;
        postInvalidate();
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        mPowerSaveEnabled = isPowerSave;
        if (!mIsAnimating) {
            invalidateSelf();
        }
    }

    private static float[] loadBoltPoints(Resources res) {
        final int[] pts = res.getIntArray(R.array.batterymeter_bolt_points);
        int maxX = 0, maxY = 0;
        for (int i = 0; i < pts.length; i += 2) {
            maxX = Math.max(maxX, pts[i]);
            maxY = Math.max(maxY, pts[i + 1]);
        }
        final float[] ptsF = new float[pts.length];
        for (int i = 0; i < pts.length; i += 2) {
            ptsF[i] = (float)pts[i] / maxX;
            ptsF[i + 1] = (float)pts[i + 1] / maxY;
        }
        return ptsF;
    }

    private static float[] loadPlusPoints(Resources res) {
        final int[] pts = res.getIntArray(R.array.batterymeter_plus_points);
        int maxX = 0, maxY = 0;
        for (int i = 0; i < pts.length; i += 2) {
            maxX = Math.max(maxX, pts[i]);
            maxY = Math.max(maxY, pts[i + 1]);
        }
        final float[] ptsF = new float[pts.length];
        for (int i = 0; i < pts.length; i += 2) {
            ptsF[i] = (float)pts[i] / maxX;
            ptsF[i + 1] = (float)pts[i + 1] / maxY;
        }
        return ptsF;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mHeight = bottom - top;
        mWidth = right - left;

        mCircleSize = Math.min(mWidth, mHeight);
        float strokeWidth = mCircleSize / STROKE_WITH;

        mWarningTextPaint.setTextSize(mCircleSize * 0.75f);
        mFramePaint.setStrokeWidth(strokeWidth);
        mFillPaint.setStrokeWidth(strokeWidth);
        mDrawRect = new RectF(strokeWidth / 2.0f, strokeWidth / 2.0f, mCircleSize
                    - strokeWidth / 2.0f, mCircleSize - strokeWidth / 2.0f);
    }

    private int getColorForLevel(int percent) {

        // If we are in power save mode, always use the normal color.
        if (mPowerSaveEnabled) {
            return mIconTint;
        }
        int thresh, color = 0;
        for (int i=0; i<mColors.length; i+=2) {
            thresh = mColors[i];
            color = mColors[i+1];
            if (percent <= thresh) {

                // Respect tinting for "normal" level
                if (i == mColors.length-2) {
                    return mIconTint;
                } else {
                    return color;
                }
            }
        }
        return color;
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
    public void setTint(int tintColor) {
        setIconColor(tintColor);
    }

    @Override
    public void setIconColor(int fillColor) {
        mFrameColor = (mFrameColor & 0xff000000) | (fillColor & 0x00ffffff);
        mIconTint = fillColor;
        invalidateSelf();
    }

    @Override
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidateSelf();

    }

    @Override
    public void setDarkIntensity(float darkIntensity, int fillColor, int fillColorDark,
            int textColor, int textColorDark) {
        int backgroundTint = getBackgroundColor(darkIntensity, fillColor, fillColorDark);
        int fillTint = getFillOrTextColor(darkIntensity, fillColor, fillColorDark);
        int textTint = getFillOrTextColor(darkIntensity, textColor, textColorDark);
        mFrameColor = backgroundTint;
        mIconTint = fillTint;
        mTextColor = textTint;
        invalidateSelf();
    }

    private int getBackgroundColor(float darkIntensity, int lightColor, int darkColor) {
        int lightModeColor = (mLightModeBackgroundColor & 0xff000000) | (lightColor & 0x00ffffff);
        int darkModeColor = (mDarkModeBackgroundColor & 0xff000000) | (darkColor & 0x00ffffff);
        return getColorForDarkIntensity(darkIntensity, lightModeColor, darkModeColor);
    }

    private int getFillOrTextColor(float darkIntensity, int lightColor, int darkColor) {
        int lightModeColor = (mLightModeFillColor & 0xff000000) | (lightColor & 0x00ffffff);
        int darkModeColor = (mDarkModeFillColor & 0xff000000) | (darkColor & 0x00ffffff);
        return getColorForDarkIntensity(darkIntensity, lightModeColor, darkModeColor);
    }

    private int getColorForDarkIntensity(float darkIntensity, int lightColor, int darkColor) {
        return (int) ArgbEvaluator.getInstance().evaluate(darkIntensity, lightColor, darkColor);
    }

    @Override
    public void setTextVisibility(boolean show) {
        mShowPercent = show;
        if (!mIsAnimating) {
            invalidateSelf();
        }
    }

    @Override
    public void setCircleDots(int interval, int length) {
        mDotInterval = interval;
        if (mDotInterval == 0) {
            mDotLength = 0;
            mIsCircleDotted = false;
        } else {
            mDotLength = length;
            mIsCircleDotted = true;
        }
        if (!mIsAnimating) {
            invalidateSelf();
        }
    }

    @Override
    public void setShowChargeAnimation(boolean showChargeAnimation) {
        mShowChargeAnimation = showChargeAnimation;
        if (mShowChargeAnimation) {
            invalidateSelf();
        }
    }

    @Override
    public void setCutOutText(boolean cutOutText) {
    }

    @Override
    public void setup(int iconColor, int textColor, boolean showText, int dotInterval, int dotLength,
            boolean showChargeAnimation, boolean cutOutText) {
        mFrameColor = (mFrameColor & 0xff000000) | (iconColor & 0x00ffffff);
        mIconTint = iconColor;
        mTextColor = textColor;
        mShowPercent = showText;
        mDotInterval = dotInterval;
        if (mDotInterval == 0) {
            mDotLength = 0;
            mIsCircleDotted = false;
        } else {
            mDotLength = dotLength;
            mIsCircleDotted = true;
        }
        mShowChargeAnimation = showChargeAnimation;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        final int level = mIsAnimating ? mAnimLevel : mLevel;

        if (level == -1) return;

        // apply frame tint
        mFramePaint.setColor(mFrameColor);
        // apply battery tint
        mFillPaint.setColor(getColorForLevel(mCharging || mIsAnimating ? 50 : level));
        // apply text tint
        mTextPaint.setColor(getTextColorForLevel(mCharging || mIsAnimating ? 50 : level));
        // apply bolt tint, (indicates charging, always use normal text color)
        mBoltPaint.setColor(getTextColorForLevel(50));
        // apply plus tint, (indicates power saving mode, always use normal text color)
        mPlusPaint.setColor(getTextColorForLevel(50));

        if (mIsCircleDotted) {
            // change mPaintStatus from solid to dashed
            mFillPaint.setPathEffect(
                    new DashPathEffect(new float[]{mDotLength,mDotInterval},0));
        } else {
            mFillPaint.setPathEffect(null);
        }

        // draw thin gray ring first
        canvas.drawArc(mDrawRect, 270, 360, false, mFramePaint);
        // draw colored arc representing charge level
        canvas.drawArc(mDrawRect, 270, level * 3.6f, false, mFillPaint);

        if (shouldDrawChargingBolt()) {
            // draw the bolt
            final float bl = (int) (mDrawRect.left + mDrawRect.width() / 3.2f);
            final float bt = (int) (mDrawRect.top + mDrawRect.height() / 4f);
            final float br = (int) (mDrawRect.right - mDrawRect.width() / 5.2f);
            final float bb = (int) (mDrawRect.bottom - mDrawRect.height() / 8f);
            if (mBoltFrame.left != bl || mBoltFrame.top != bt
                    || mBoltFrame.right != br || mBoltFrame.bottom != bb) {
                mBoltFrame.set(bl, bt, br, bb);
                mBoltPath.reset();
                mBoltPath.moveTo(
                        mBoltFrame.left + mBoltPoints[0] * mBoltFrame.width(),
                        mBoltFrame.top + mBoltPoints[1] * mBoltFrame.height());
                for (int i = 2; i < mBoltPoints.length; i += 2) {
                    mBoltPath.lineTo(
                            mBoltFrame.left + mBoltPoints[i] * mBoltFrame.width(),
                            mBoltFrame.top + mBoltPoints[i + 1] * mBoltFrame.height());
                }
                mBoltPath.lineTo(
                        mBoltFrame.left + mBoltPoints[0] * mBoltFrame.width(),
                        mBoltFrame.top + mBoltPoints[1] * mBoltFrame.height());
            }
            canvas.drawPath(mBoltPath, mBoltPaint);
        } else if (mPowerSaveEnabled) {
            // define the plus shape
            final float pw = mDrawRect.width() * 2 / 3;
            final float pl = mDrawRect.left + (mDrawRect.width() - pw) / 2;
            final float pt = mDrawRect.top + (mDrawRect.height() - pw) / 2;
            final float pr = mDrawRect.right - (mDrawRect.width() - pw) / 2;
            final float pb = mDrawRect.bottom - (mDrawRect.height() - pw) / 2;
            if (mPlusFrame.left != pl || mPlusFrame.top != pt
                    || mPlusFrame.right != pr || mPlusFrame.bottom != pb) {
                mPlusFrame.set(pl, pt, pr, pb);
                mPlusPath.reset();
                mPlusPath.moveTo(
                        mPlusFrame.left + mPlusPoints[0] * mPlusFrame.width(),
                        mPlusFrame.top + mPlusPoints[1] * mPlusFrame.height());
                for (int i = 2; i < mPlusPoints.length; i += 2) {
                    mPlusPath.lineTo(
                            mPlusFrame.left + mPlusPoints[i] * mPlusFrame.width(),
                            mPlusFrame.top + mPlusPoints[i + 1] * mPlusFrame.height());
                }
                mPlusPath.lineTo(
                        mPlusFrame.left + mPlusPoints[0] * mPlusFrame.width(),
                        mPlusFrame.top + mPlusPoints[1] * mPlusFrame.height());
            }
            canvas.drawPath(mPlusPath, mPlusPaint);
        } else if (mShowPercent) {
            if (mLevel <= mCriticalLevel && !mPluggedIn) {
                // draw the warning text
                canvas.drawText(mWarningString, mTextX, mTextY, mWarningTextPaint);
            } else {
                // draw the percentage text
                String pctText = String.valueOf(mLevel);
                updatePercentageTextSize(pctText);
                canvas.drawText(pctText, mTextX, mTextY, mTextPaint);
            }
        }
    }

    // Some stuff required by Drawable.
    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    private boolean shouldDrawChargingBolt() {
        return mPluggedIn && mCharging && (!mShowPercent || !mIsAnimating);
    }

    /**
     * initializes all size dependent variables
     * sets stroke width and text size of all involved paints
     * YES! i think the method name is appropriate
     */
    private void updatePercentageTextSize(String level) {
        mTextPaint.setTextSize(mCircleSize * (SINGLE_DIGIT_PERCENT
                ? 0.75f : (mLevel == LEVEL_FULL ? 0.38f : 0.5f)));

        Rect bounds = new Rect();
        float strokeWidth = mCircleSize / STROKE_WITH;
        mTextPaint.getTextBounds(level, 0, level.length(), bounds);
        mTextX = mCircleSize / 2.0f;
        mTextY = mCircleSize / 2.0f + (bounds.bottom - bounds.top) / 2.0f
                - strokeWidth / 2.0f + mContext.getResources().getDisplayMetrics().density;
    }
}
