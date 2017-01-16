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

import android.annotation.Nullable;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class BatteryMeterBaseDrawable extends Drawable {
    public static boolean SINGLE_DIGIT_PERCENT = false;

    public static final int LEVEL_FULL = 100;
    public static final int FULL       = 96;

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
    }
    public void onBatteryLevelChanged(int level, int animLevel, boolean pluggedIn, boolean charging) {
    }
    public void onPowerSaveChanged(boolean isPowerSave) {
    }
    public void setIconColor(int fillColor) {
    }
    public void setTextColor(int textColor) {
    }
    public void setDarkIntensity(float darkIntensity, int fillColor, int fillColorDark,
            int textColor, int textColorDark) {
    }
    public void setTextVisibility(boolean show) {
    }
    public void setCircleDots(int interval, int length) {
    }
    public void setShowChargeAnimation(boolean showChargeAnimation) {
    }
    public void setCutOutText(boolean cutOutText) {
    }
    public void setup(int iconColor, int textColor, boolean showText, int dotInterval, int dotLength,
            boolean showChargeAnimation, boolean cutOutText) {
    }
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
    @Override
    public void draw(Canvas canvas) {
    }
}
