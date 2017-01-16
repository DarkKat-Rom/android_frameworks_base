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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;

public class BatteryMeterDrawable extends BatteryMeterBaseDrawable  {
    private static final float ASPECT_RATIO = 9.5f / 14.5f;

    private static final float BOLT_LEVEL_THRESHOLD = 0.3f;  // opaque bolt below this fraction

    private final Context mContext;
    private final Handler mHandler;

    private int mHeight;
    private int mWidth;
    private float mTextHeight;
    private float mWarningTextHeight;
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

    private final Path mShapePath = new Path();
    private final Path mClipPath = new Path();
    private final Path mTextPath = new Path();
    private final Path mBoltPath = new Path();
    private final Path mPlusPath = new Path();

    private final RectF mFrame = new RectF();
    private final RectF mButtonFrame = new RectF();
    private final RectF mBoltFrame = new RectF();
    private final RectF mPlusFrame = new RectF();

    private final float mButtonHeightFraction;
    private final float mSubpixelSmoothingLeft;
    private final float mSubpixelSmoothingRight;

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

    private final float mShapeRotationDegrees;
    private final float mTextRotationDegrees;
    private boolean mRotate;

    private boolean mShowPercent = false;
    private boolean mShowChargeAnimation = false;
    private boolean mCutOutText = true;

    public BatteryMeterDrawable(Context context, Handler handler, float shapeRotationDegrees,
                float textRotationDegrees) {
        mContext = context;
        mHandler = handler;
        final Resources res = mContext.getResources();

        mIntrinsicHeight = res.getDimensionPixelSize(R.dimen.battery_height);
        mIntrinsicWidth = res.getDimensionPixelSize(R.dimen.battery_width);

        mWarningString = context.getString(R.string.battery_meter_very_low_overlay_symbol);
        mCriticalLevel = mContext.getResources().getInteger(
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
        mFramePaint.setDither(true);
        mFramePaint.setStrokeWidth(0);
        mFramePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setDither(true);
        mFillPaint.setStrokeWidth(0);
        mFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);

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

        mButtonHeightFraction = context.getResources().getFraction(
                R.fraction.battery_button_height_fraction, 1, 1);
        mSubpixelSmoothingLeft = context.getResources().getFraction(
                R.fraction.battery_subpixel_smoothing_left, 1, 1);
        mSubpixelSmoothingRight = context.getResources().getFraction(
                R.fraction.battery_subpixel_smoothing_right, 1, 1);

        mDarkModeBackgroundColor =
                context.getColor(R.color.dark_mode_icon_color_dual_tone_background);
        mDarkModeFillColor = context.getColor(R.color.dark_mode_icon_color_dual_tone_fill);
        mLightModeBackgroundColor =
                context.getColor(R.color.light_mode_icon_color_dual_tone_background);
        mLightModeFillColor = context.getColor(R.color.light_mode_icon_color_dual_tone_fill);

        mShapeRotationDegrees = shapeRotationDegrees;
        mTextRotationDegrees = textRotationDegrees;
        mRotate = mShapeRotationDegrees != BatteryMeterView.ROTATION_NONE
                && mTextRotationDegrees != BatteryMeterView.ROTATION_NONE;
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
        mWarningTextPaint.setTextSize(mHeight * 0.75f);
        mWarningTextHeight = -mWarningTextPaint.getFontMetrics().ascent;
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
        mCutOutText = cutOutText;
        if (!mIsAnimating) {
            invalidateSelf();
        }
    }

    @Override
    public void setup(int iconColor, int textColor, boolean showText, int dotInterval, int dotLength,
            boolean showChargeAnimation, boolean cutOutText) {
        mFrameColor = (mFrameColor & 0xff000000) | (iconColor & 0x00ffffff);
        mIconTint = iconColor;
        mTextColor = textColor;
        mShowPercent = showText;
        mShowChargeAnimation = showChargeAnimation;
        mCutOutText = cutOutText;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas c) {
        final int level = mIsAnimating ? mAnimLevel : mLevel;

        if (level == -1) return;

        float drawFrac = (float) level / 100f;
        final int height = mHeight;
        final int width = (int) (ASPECT_RATIO * mHeight);
        int px = (mWidth - width) / 2;
        final float rotateX = mWidth / 2;
        final float rotateY = mHeight / 2;

        final int buttonHeight = (int) (height * mButtonHeightFraction);

        mFrame.set(0, 0, width, height);
        mFrame.offset(px, 0);

        // button-frame: area above the battery body
        mButtonFrame.set(
                mFrame.left + Math.round(width * 0.25f),
                mFrame.top,
                mFrame.right - Math.round(width * 0.25f),
                mFrame.top + buttonHeight);

        mButtonFrame.top += mSubpixelSmoothingLeft;
        mButtonFrame.left += mSubpixelSmoothingLeft;
        mButtonFrame.right -= mSubpixelSmoothingRight;

        // frame: battery body area
        mFrame.top += buttonHeight;
        mFrame.left += mSubpixelSmoothingLeft;
        mFrame.top += mSubpixelSmoothingLeft;
        mFrame.right -= mSubpixelSmoothingRight;
        mFrame.bottom -= mSubpixelSmoothingRight;

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

        if (level >= FULL) {
            drawFrac = 1f;
        } else if (level <= mCriticalLevel) {
            drawFrac = 0f;
        }

        final float levelTop = drawFrac == 1f ? mButtonFrame.top
                : (mFrame.top + (mFrame.height() * (1f - drawFrac)));

        // define the battery shape
        mShapePath.reset();
        mShapePath.moveTo(mButtonFrame.left, mButtonFrame.top);
        mShapePath.lineTo(mButtonFrame.right, mButtonFrame.top);
        mShapePath.lineTo(mButtonFrame.right, mFrame.top);
        mShapePath.lineTo(mFrame.right, mFrame.top);
        mShapePath.lineTo(mFrame.right, mFrame.bottom);
        mShapePath.lineTo(mFrame.left, mFrame.bottom);
        mShapePath.lineTo(mFrame.left, mFrame.top);
        mShapePath.lineTo(mButtonFrame.left, mFrame.top);
        mShapePath.lineTo(mButtonFrame.left, mButtonFrame.top);

        boolean pctOpaque = false;
        float pctX = 0, pctY = 0;
        String pctText = null;
        boolean drawBolt = false;
        boolean drawPlus = false;
        boolean drawWarningText = false;
        boolean drawPercentageText = false;

        if (shouldDrawChargingBolt()) {
            // define the bolt shape
            final float bl = mFrame.left + mFrame.width() / 4f;
            final float bt = mFrame.top + mFrame.height() / 6f;
            final float br = mFrame.right - mFrame.width() / 4f;
            final float bb = mFrame.bottom - mFrame.height() / 10f;
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

            float boltPct = (mBoltFrame.bottom - levelTop) / (mBoltFrame.bottom - mBoltFrame.top);
            boltPct = Math.min(Math.max(boltPct, 0), 1);
            if (mCutOutText) {
                if (boltPct <= BOLT_LEVEL_THRESHOLD) {
                    drawBolt = true;
                } else {
                    // Cut the bolt out of the overall shape
                    mShapePath.op(mBoltPath, Path.Op.DIFFERENCE);
                }
            } else {
                drawBolt = true;
            }
        } else if (mPowerSaveEnabled) {
            // define the plus shape
            final float pw = mFrame.width() * 2 / 3;
            final float pl = mFrame.left + (mFrame.width() - pw) / 2;
            final float pt = mFrame.top + (mFrame.height() - pw) / 2;
            final float pr = mFrame.right - (mFrame.width() - pw) / 2;
            final float pb = mFrame.bottom - (mFrame.height() - pw) / 2;
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

            float plusPct = (mPlusFrame.bottom - levelTop) / (mPlusFrame.bottom - mPlusFrame.top);
            plusPct = Math.min(Math.max(plusPct, 0), 1);

            if (mCutOutText) {
                if (plusPct <= BOLT_LEVEL_THRESHOLD) {
                    drawPlus = true;
                } else {
                    // Cut the plus out of the overall shape
                    mShapePath.op(mPlusPath, Path.Op.DIFFERENCE);
                }
            } else {
                drawPlus = true;
            }
        } else if (mShowPercent) {
            // compute percentage text
            if (mLevel <= mCriticalLevel && !mPluggedIn) {
                drawWarningText = true;
            } else {
                mTextPaint.setTextSize(height *
                        (SINGLE_DIGIT_PERCENT ? 0.75f
                                : (mLevel == LEVEL_FULL ? 0.38f : 0.5f)));
                mTextHeight = -mTextPaint.getFontMetrics().ascent;
                pctText = String.valueOf(mLevel);
                pctX = mWidth * 0.5f;
                pctY = (mHeight + mTextHeight) * 0.47f;
                pctOpaque = levelTop > pctY;
                if (!pctOpaque && mCutOutText) {
                    mTextPath.reset();
                    mTextPaint.getTextPath(pctText, 0, pctText.length(), pctX, pctY, mTextPath);
                    if (mRotate) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(mTextRotationDegrees, rotateX, rotateY);
                        mTextPath.transform(matrix);
                    }
                    // Cut the percentage text out of the overall shape
                    mShapePath.op(mTextPath, Path.Op.DIFFERENCE);
                } else {
                   drawPercentageText = true; 
                }
            }
        }

        if (mRotate) {
            c.save(Canvas.MATRIX_SAVE_FLAG);
            c.rotate(mShapeRotationDegrees, rotateX, rotateY);
        }
        // draw the battery shape background
        c.drawPath(mShapePath, mFramePaint);

        // draw the battery shape, clipped to charging level
        mFrame.top = levelTop;
        mClipPath.reset();
        mClipPath.addRect(mFrame,  Path.Direction.CCW);
        mShapePath.op(mClipPath, Path.Op.INTERSECT);
        c.drawPath(mShapePath, mFillPaint);

        if (drawBolt) {
            c.drawPath(mBoltPath, mBoltPaint);
        } else if (drawPlus) {
            c.drawPath(mPlusPath, mPlusPaint);
        } else if (drawWarningText) {
            final float x = mWidth * 0.5f;
            final float y = (mHeight + mWarningTextHeight) * 0.48f;
            c.drawText(mWarningString, x, y, mWarningTextPaint);
        } else if (drawPercentageText) {
            if (mRotate) {
                c.restore();
            }
            c.drawText(pctText, pctX, pctY, mTextPaint);
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
}
