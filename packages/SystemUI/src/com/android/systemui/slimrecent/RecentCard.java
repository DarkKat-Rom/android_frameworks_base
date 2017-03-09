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

package com.android.systemui.slimrecent;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.Settings;

import com.android.internal.util.darkkat.ColorConstants;
import com.android.internal.util.darkkat.ColorHelper;
import com.android.internal.util.darkkat.SlimRecentsColorHelper;
import com.android.internal.util.darkkat.ThemeHelper;
import com.android.systemui.R;
import com.android.systemui.recents.misc.Utilities;

public class RecentCard {

    private static final boolean DEBUG = false;

    private final Context mContext;
    private TaskDescription mTaskDescription;
    private int mPersistentTaskId;

    private boolean mForceThumbnailUpdate = false;

    private Bitmap mAppIcon;
    private String mLabel;
    private int mBackgroundColor;
    private ColorStateList mDividerTint;
    private int mHeaderTextColor;
    private ColorStateList mActionIconTint;
    private ColorStateList mRippleColor;

    public RecentCard(Context context, TaskDescription td) {

        mContext = context;
        mTaskDescription = td;
        mPersistentTaskId = td.persistentTaskId;

        setForceThumbnailUpdate(true);
        setColors();
    }

    public void updateCardContent(TaskDescription td) {
        mTaskDescription = td;
        mPersistentTaskId = td.persistentTaskId;

        setForceThumbnailUpdate(true);
        setColors();
    }

    public void setColors() {
        setBackgroundColor();
        setRippleColor();
        setHeaderTextColor();
        setActionIconColor();
    }

    public void setBackgroundColor() {
        mBackgroundColor = resolveBackgroundColor();
        mRippleColor = ColorStateList.valueOf(resolveRippleColor());
        mHeaderTextColor = resolveHeaderTextColor();
        mActionIconTint = ColorStateList.valueOf(resolveActionIconColor());
        mDividerTint = ColorStateList.valueOf((31  << 24) | (resolveActionIconColor() & 0x00ffffff));
    }

    public void setRippleColor() {
        mRippleColor = ColorStateList.valueOf(resolveRippleColor());
    }

    public void setHeaderTextColor() {
        mHeaderTextColor = resolveHeaderTextColor();
    }

    public void setActionIconColor() {
        mActionIconTint = ColorStateList.valueOf(resolveActionIconColor());
        mDividerTint = ColorStateList.valueOf((31  << 24) | (resolveActionIconColor() & 0x00ffffff));
    }

    public void setTaskDescription(TaskDescription td) {
        mTaskDescription = td;
        mPersistentTaskId = td.persistentTaskId;
    }

    public TaskDescription getTaskDescription() {
        return mTaskDescription;
    }

    public int getPersistentTaskId() {
        return mPersistentTaskId;
    }

    public String getLabel() {
        return mTaskDescription.getLabel();
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public ColorStateList getRippleColor() {
        return mRippleColor;
    }

    public ColorStateList getThumbnailFrameTint() {
        return ColorStateList.valueOf(
                (31  << 24) | (resolveThumbnailFrameColor(mBackgroundColor) & 0x00ffffff));
    }

    public ColorStateList getDividerTint() {
        return mDividerTint;
    }

    public int getHeaderTextColor() {
        return mHeaderTextColor;
    }

    public ColorStateList getActionIconTint() {
        return mActionIconTint;
    }

    public boolean isFavorite() {
        return getTaskDescription().getIsFavorite();
    }

    private boolean cardUseAutoColors() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SLIM_RECENTS_CARD_USE_AUTO_COLORS, 1) == 1;
    }

    private int resolveBackgroundColor() {
        if (mTaskDescription != null && mTaskDescription.cardColor != 0
                && cardUseAutoColors()) {
            return mTaskDescription.cardColor;
        }
        return SlimRecentsColorHelper.getCardBackgroundColor(mContext);
    }

    public int resolveRippleColor() {
        if (mTaskDescription != null && mTaskDescription.cardColor != 0
                && cardUseAutoColors()) {
            if (Utilities.computeContrastBetweenColors(mTaskDescription.cardColor,
                    Color.WHITE) < 3f) {
                return mContext.getResources().getColor(
                        R.color.recents_ripple_color_dark);
            } else {
                return mContext.getResources().getColor(
                        R.color.recents_ripple_color_light);
            }
        }
        return SlimRecentsColorHelper.getCardRippleColor(mContext);
    }

    public int resolveThumbnailFrameColor(int backgroundColor) {
        int color = ColorConstants.BLACK;
        if (ColorHelper.isColorDark(backgroundColor)) {
            color = ColorConstants.WHITE;
        }
        return color;
    }

    public int resolveHeaderTextColor() {
        if (mTaskDescription != null && mTaskDescription.cardColor != 0
                && cardUseAutoColors()) {
            if (Utilities.computeContrastBetweenColors(mTaskDescription.cardColor,
                    Color.WHITE) < 3f) {
                return mContext.getResources().getColor(
                        R.color.recents_header_text_color_dark);
            } else {
                return mContext.getResources().getColor(
                        R.color.recents_header_text_color_light);
            }
        }
        return SlimRecentsColorHelper.getCardHeaderTextColor(mContext);
    }

    public int resolveActionIconColor() {
        if (mTaskDescription != null && mTaskDescription.cardColor != 0
                && cardUseAutoColors()) {
            if (Utilities.computeContrastBetweenColors(mTaskDescription.cardColor,
                    Color.WHITE) < 3f) {
                return mContext.getResources().getColor(
                        R.color.recents_header_icon_color_dark);
            } else {
                return mContext.getResources().getColor(
                        R.color.recents_header_icon_color_light);
            }
        }
        return SlimRecentsColorHelper.getCardActionIconColor(mContext);
    }

    public void loadAppicon(RecentImageView appIconView) {
        final Bitmap appIcon =
                CacheController.getInstance(mContext).getBitmapFromMemCache(mTaskDescription.identifier);
        if (appIcon == null) {
            AppIconLoader.getInstance(mContext).loadAppIcon(
                    mTaskDescription.resolveInfo, mTaskDescription.identifier, appIconView);
        } else {
            appIconView.setImageBitmap(appIcon);
        }
    }

    public void setForceThumbnailUpdate(boolean force) {
        mForceThumbnailUpdate = force;
    }

    public void loadThumbnail(RecentImageView thumbnailView, float aspectRatio) {
        final Bitmap thumbnail = CacheController.getInstance(mContext)
                        .getBitmapFromMemCache(String.valueOf(mPersistentTaskId));
        if (thumbnail == null || mForceThumbnailUpdate) {
            ThumbnailLoader.getInstance(mContext).loadThumbnail(mPersistentTaskId, thumbnailView,
                    aspectRatio);
            if (mForceThumbnailUpdate) {
                mForceThumbnailUpdate = false;
            }
        } else {
            thumbnailView.setImageBitmap(thumbnail);
        }
    }
}
