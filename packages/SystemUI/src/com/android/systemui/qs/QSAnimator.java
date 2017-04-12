/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.android.systemui.qs.PagedTileLayout.PageListener;
import com.android.systemui.qs.QSPanel.QSTileLayout;
import com.android.systemui.qs.QSTile.Host.Callback;
import com.android.systemui.qs.TouchAnimator.Builder;
import com.android.systemui.qs.TouchAnimator.Listener;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.QSTileHost;

import java.util.ArrayList;
import java.util.Collection;

public class QSAnimator implements Callback, PageListener, Listener, OnLayoutChangeListener,
        OnAttachStateChangeListener {

    private static final String TAG = "QSAnimator";

    private static final String ALLOW_FANCY_ANIMATION = "sysui_qs_fancy_anim";
    private static final String MOVE_FULL_ROWS = "sysui_qs_move_whole_rows";

    public static final float EXPANDED_TILE_DELAY = .86f;

    public static final int BRIGHTNESS_SLIDER_SHOW_ON_QS_PANEL = 2;
    public static final int BRIGHTNESS_SLIDER_HIDDEN           = 3;

    private final Context mContext;
    private final Resources mResources;
    private final ArrayList<View> mAllViews = new ArrayList<>();
    private final ArrayList<View> mTopFiveQs = new ArrayList<>();
    private final QSContainer mQsContainer;
    private final BaseStatusBarHeader mHeader;
    private final HorizontalScrollView mQuickQsPanelScroller;
    private final QuickQSPanel mQuickQsPanel;
    private final QSPanel mQsPanel;

    private PagedTileLayout mPagedLayout;

    private boolean mOnFirstPage = true;
    private TouchAnimator mFirstPageAnimator;
    private TouchAnimator mFirstPageDelayedAnimator;
    private TouchAnimator mTranslationXAnimator;
    private TouchAnimator mTranslationYAnimator;
    private TouchAnimator mNonfirstPageAnimator;

    private boolean mOnKeyguard;

    private boolean mAllowFancy = true;
    private boolean mFullRows = true;
    private float mLastPosition;
    private QSTileHost mHost;

    private final int mHeaderHeight;
    private final int mHeaderHeightWithBrightnessView;
    private final int mHeaderExpandedHeight;
    private final int mHeaderExpandedHeightWithBrightnessView;


    private int mBrightnesSliderVisibility = BRIGHTNESS_SLIDER_SHOW_ON_QS_PANEL;

    public QSAnimator(Context context, QSContainer container, BaseStatusBarHeader header,
            HorizontalScrollView quickPanelScroller, QuickQSPanel quickPanel, QSPanel panel) {
        mContext = context;
        mResources = mContext.getResources();
        mQsContainer = container;
        mHeader = header;
        mQuickQsPanelScroller = quickPanelScroller;
        mQuickQsPanel = quickPanel;
        mQsPanel = panel;
        mQsPanel.addOnAttachStateChangeListener(this);
        mQsContainer.addOnLayoutChangeListener(this);
        QSTileLayout tileLayout = mQsPanel.getTileLayout();
        if (tileLayout instanceof PagedTileLayout) {
            mPagedLayout = ((PagedTileLayout) tileLayout);
            mPagedLayout.setPageListener(this);
        } else {
            Log.w(TAG, "QS Not using page layout");
        }

        mHeaderHeight = mResources.getDimensionPixelSize(R.dimen.status_bar_header_height);
        mHeaderHeightWithBrightnessView = mResources.getDimensionPixelSize(
                R.dimen.status_bar_header_with_brightness_view_height);
        mHeaderExpandedHeight =
                mResources.getDimensionPixelSize(R.dimen.status_bar_header_expanded_height);
        mHeaderExpandedHeightWithBrightnessView = mResources.getDimensionPixelSize(
                R.dimen.status_bar_header_with_brightness_view_expanded_height);
    }

    public void onRtlChanged() {
        updateAnimators();
    }

    public void setOnKeyguard(boolean onKeyguard) {
        mOnKeyguard = onKeyguard;
        updateHeaderHeight();
        mQuickQsPanel.setVisibility(mOnKeyguard ? View.INVISIBLE : View.VISIBLE);
        if (mOnKeyguard) {
            clearAnimationState();
        }
    }

    public void setHost(QSTileHost qsh) {
        mHost = qsh;
        qsh.addCallback(this);
        updateAnimators();
    }

    @Override
    public void onViewAttachedToWindow(View v) {
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (mHost != null) {
            mHost.removeCallback(this);
        }
    }

    @Override
    public void onPageChanged(boolean isFirst) {
        if (mOnFirstPage == isFirst) return;
        if (!isFirst) {
            clearAnimationState();
        }
        mOnFirstPage = isFirst;
    }

    private void updateAnimators() {
        TouchAnimator.Builder firstPageBuilder = new Builder();
        TouchAnimator.Builder translationXBuilder = new Builder();
        TouchAnimator.Builder translationYBuilder = new Builder();

        if (mQsPanel.getHost() == null) return;
        Collection<QSTile<?>> tiles = mQsPanel.getHost().getTiles();
        int count = 0;
        int[] loc1 = new int[2];
        int[] loc2 = new int[2];
        int lastXDiff = 0;
        int lastX = 0;

        clearAnimationState();
        mAllViews.clear();
        mTopFiveQs.clear();

        mAllViews.add((View) mQsPanel.getTileLayout());

        for (QSTile<?> tile : tiles) {
            QSTileBaseView tileView = mQsPanel.getTileView(tile);
            if (tileView == null) {
                Log.e(TAG, "tileView is null " + tile.getTileSpec());
                continue;
            }
            final TextView label = ((QSTileView) tileView).getLabel();
            final View tileIcon = tileView.getIcon().getIconView();
            if (count < mQuickQsPanel.getNumQuickTiles() && mAllowFancy) {
                // Quick tiles.
                QSTileBaseView quickTileView = mQuickQsPanel.getTileView(tile);
                if (quickTileView == null) {
                    continue;
                }

                lastX = loc1[0];
                getRelativePosition(loc1, quickTileView.getIcon(), mQsContainer);
                getRelativePosition(loc2, tileIcon, mQsContainer);
                final int xDiff = loc2[0] - loc1[0];
                final int yDiff = loc2[1] - loc1[1];
                lastXDiff = loc1[0] - lastX;
                // Move the quick tile right from its location to the new one.
                translationXBuilder.addFloat(quickTileView, "translationX", 0, xDiff);
                translationYBuilder.addFloat(quickTileView, "translationY", 0, yDiff);

                // Counteract the parent translation on the tile. So we have a static base to
                // animate the label position off from.
                firstPageBuilder.addFloat(tileView, "translationY", mQsPanel.getHeight(), 0);

                // Move the real tile's label from the quick tile position to its final
                // location.
                translationXBuilder.addFloat(label, "translationX", -xDiff, 0);
                translationYBuilder.addFloat(label, "translationY", -yDiff, 0);

                mTopFiveQs.add(tileView.getIcon());
                mAllViews.add(tileView.getIcon());
                mAllViews.add(quickTileView);
            } else if (mFullRows && isIconInAnimatedRow(count)) {
                // TODO: Refactor some of this, it shares a lot with the above block.
                // Move the last tile position over by the last difference between quick tiles.
                // This makes the extra icons seems as if they are coming from positions in the
                // quick panel.
                loc1[0] += lastXDiff;
                getRelativePosition(loc2, tileIcon, mQsContainer);
                final int xDiff = loc2[0] - loc1[0];
                final int yDiff = loc2[1] - loc1[1];

                firstPageBuilder.addFloat(tileView, "translationY", mQsPanel.getHeight(), 0);
                translationXBuilder.addFloat(tileView, "translationX", -xDiff, 0);
                translationYBuilder.addFloat(label, "translationY", -yDiff, 0);
                translationYBuilder.addFloat(tileIcon, "translationY", -yDiff, 0);

                mAllViews.add(tileIcon);
            } else {
                QSTileBaseView quickTileView = mQuickQsPanel.getTileView(tile);
                if (quickTileView != null) {
                    mAllViews.add(quickTileView);
                }
            }
            mAllViews.add(tileView);
            mAllViews.add(label);
            count++;
        }
        if (mAllowFancy) {
            mFirstPageAnimator = firstPageBuilder
                    .setListener(this)
                    .build();
            // Fade in the tiles/labels as we reach the final position.
            mFirstPageDelayedAnimator = new TouchAnimator.Builder()
                    .setStartDelay(EXPANDED_TILE_DELAY)
                    .addFloat(mQsPanel.getTileLayout(), "alpha", 0, 1)
                    .addFloat(mQsPanel.getFooter().getView(), "alpha", 0, 1).build();
            mAllViews.add(mQsPanel.getFooter().getView());
            float px = 0;
            float py = 1;
            if (tiles.size() <= 3) {
                px = 1;
            } else if (tiles.size() <= 6) {
                px = .4f;
            }
            PathInterpolatorBuilder interpolatorBuilder = new PathInterpolatorBuilder(0, 0, px, py);
            translationXBuilder.setInterpolator(interpolatorBuilder.getXInterpolator());
            translationYBuilder.setInterpolator(interpolatorBuilder.getYInterpolator());
            mTranslationXAnimator = translationXBuilder.build();
            mTranslationYAnimator = translationYBuilder.build();
        } else {
            mFirstPageAnimator = new TouchAnimator.Builder()
                .addFloat(mQuickQsPanel, "alpha", 1, 0)
                .setListener(mNonFirstPageListener)
                .setEndDelay(.5f)
                .build();
        }
        mNonfirstPageAnimator = new TouchAnimator.Builder()
                .addFloat(mQuickQsPanel, "alpha", 1, 0)
                .setListener(mNonFirstPageListener)
                .setEndDelay(.5f)
                .build();
    }

    private boolean isIconInAnimatedRow(int count) {
        if (mPagedLayout == null) {
            return false;
        }
        final int columnCount = mPagedLayout.getColumnCount();
        return count < ((mQuickQsPanel.getNumQuickTiles() + columnCount - 1) / columnCount) * columnCount;
    }

    private void getRelativePosition(int[] loc1, View view, View parent) {
        loc1[0] = 0 + view.getWidth() / 2;
        loc1[1] = 0;
        getRelativePositionInt(loc1, view, parent);
    }

    private void getRelativePositionInt(int[] loc1, View view, View parent) {
        if(view == parent || view == null) return;
        // Ignore tile pages as they can have some offset we don't want to take into account in
        // RTL.
        if (!(view instanceof PagedTileLayout.TilePage)) {
            loc1[0] += view.getLeft();
            loc1[1] += view.getTop();
        }
        getRelativePositionInt(loc1, (View) view.getParent(), parent);
    }

    public void setPosition(float position) {
        if (mFirstPageAnimator == null) return;
        if (mOnKeyguard) {
            return;
        }
        mLastPosition = position;
        if (mOnFirstPage && mAllowFancy) {
            mQuickQsPanel.setAlpha(1);
            mFirstPageAnimator.setPosition(position);
            mFirstPageDelayedAnimator.setPosition(position);
            mTranslationXAnimator.setPosition(position);
            mTranslationYAnimator.setPosition(position);
        } else {
            mNonfirstPageAnimator.setPosition(position);
        }
    }

    @Override
    public void onAnimationAtStart() {
        updateHeaderHeight();
        mQuickQsPanel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationAtEnd() {
        updateHeaderHeight();
        mQuickQsPanel.setVisibility(View.INVISIBLE);
        final int N = mTopFiveQs.size();
        for (int i = 0; i < N; i++) {
            mTopFiveQs.get(i).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationStarted() {
        updateHeaderHeight();
        mQuickQsPanel.setVisibility(mOnKeyguard ? View.INVISIBLE : View.VISIBLE);
        if (mOnFirstPage) {
            final int N = mTopFiveQs.size();
            for (int i = 0; i < N; i++) {
                mTopFiveQs.get(i).setVisibility(View.INVISIBLE);
            }
        }
    }

    private void clearAnimationState() {
        final int N = mAllViews.size();
        mQuickQsPanel.setAlpha(0);
        for (int i = 0; i < N; i++) {
            View v = mAllViews.get(i);
            v.setAlpha(1);
            v.setTranslationX(0);
            v.setTranslationY(0);
        }
        final int N2 = mTopFiveQs.size();
        for (int i = 0; i < N2; i++) {
            mTopFiveQs.get(i).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        mQsPanel.post(mUpdateAnimators);
    }

    @Override
    public void onTilesChanged() {
        // Give the QS panels a moment to generate their new tiles, then create all new animators
        // hooked up to the new views.
        mQsPanel.post(mUpdateAnimators);
    }

    public void updateBrightnesSliderVisibility(int visibility) {
        mBrightnesSliderVisibility = visibility;
        updateHeaderHeight();

        int quickQsScrollContainerMarginTop = mResources.getDimensionPixelSize(alwaysShowBrightnesSlider()
                ? R.dimen.qs_bar_with_brightness_view_margin_top : R.dimen.qs_bar_margin_top);
        int qsPanelMarginTop = mResources.getDimensionPixelSize(alwaysShowBrightnesSlider()
                ? R.dimen.qs_panel_with_brightness_view_margin_top : R.dimen.qs_panel_margin_top);
        View brightnessView = mHeader.getBrightnessView();
        View headerQsPanelScrollContainer = mHeader.findViewById(R.id.quick_qs_panel_scroll_container);

        RelativeLayout.LayoutParams quickQsScrollContainerLp =
                (RelativeLayout.LayoutParams) headerQsPanelScrollContainer.getLayoutParams();
        FrameLayout.LayoutParams qsPanelLp = (FrameLayout.LayoutParams) mQsPanel.getLayoutParams();
        quickQsScrollContainerLp.topMargin = quickQsScrollContainerMarginTop;
        qsPanelLp.topMargin = qsPanelMarginTop;
        headerQsPanelScrollContainer.setLayoutParams(quickQsScrollContainerLp);
        mQsPanel.setLayoutParams(qsPanelLp);

        if (alwaysShowBrightnesSlider()) {
            if (brightnessView.getVisibility() == View.INVISIBLE) {
                brightnessView.setVisibility(View.VISIBLE);
            }
        } else {
            if (brightnessView.getVisibility() == View.VISIBLE) {
                brightnessView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateHeaderHeight() {
        int headerHeight = mOnKeyguard || mLastPosition == 1
                ? (alwaysShowBrightnesSlider()
                        ? mHeaderExpandedHeightWithBrightnessView : mHeaderExpandedHeight)
                : (alwaysShowBrightnesSlider()
                        ? mHeaderHeightWithBrightnessView : mHeaderHeight);
        FrameLayout.LayoutParams headerLp = (FrameLayout.LayoutParams) mHeader.getLayoutParams();
        headerLp.height = headerHeight;
        mHeader.setLayoutParams(headerLp);
    }

    private boolean alwaysShowBrightnesSlider() {
        return mBrightnesSliderVisibility != BRIGHTNESS_SLIDER_HIDDEN;
    }

    private final TouchAnimator.Listener mNonFirstPageListener =
            new TouchAnimator.ListenerAdapter() {
                @Override
                public void onAnimationStarted() {
                    mQuickQsPanel.setVisibility(View.VISIBLE);
                }
            };

    private Runnable mUpdateAnimators = new Runnable() {
        @Override
        public void run() {
            updateAnimators();
            setPosition(mLastPosition);
        }
    };

    public void setFancyAnimaton(boolean value) {
        mAllowFancy = value;
        mFullRows = value;
    }
}
