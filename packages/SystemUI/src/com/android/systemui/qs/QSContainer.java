/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.darkkat.util.QSColorHelper;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.stack.StackStateAnimator;

/**
 * Wrapper view with background which contains {@link QSPanel} and {@link BaseStatusBarHeader}
 *
 * Also manages animations for the QS Header and Panel.
 */
public class QSContainer extends FrameLayout {
    private static final String TAG = "QSContainer";
    private static final boolean DEBUG = false;

    public static final int BRIGHTNESS_SLIDER_SHOW             = 0;
    public static final int BRIGHTNESS_SLIDER_SHOW_ON_QS_BAR   = 1;
    public static final int BRIGHTNESS_SLIDER_SHOW_ON_QS_PANEL = 2;
    public static final int BRIGHTNESS_SLIDER_HIDDEN           = 3;

    private final Point mSizePoint = new Point();
    private final Rect mQsBounds = new Rect();

    private int mHeightOverride = -1;
    protected QSPanel mQSPanel;
    private QSDetail mQSDetail;
    protected BaseStatusBarHeader mHeader;
    private ViewGroup mQuickQsPanelScrollerContainer;
    private HorizontalScrollView mQuickQsPanelScroller;
    private QuickQSPanel mQuickQSPanel;
    protected float mQsExpansion;
    private boolean mQsExpanded;
    private boolean mHeaderAnimating;
    private boolean mKeyguardShowing;
    private boolean mStackScrollerOverscrolling;

    private long mDelay;
    private QSAnimator mQSAnimator;
    private QSCustomizer mQSCustomizer;
    private NotificationPanelView mPanelView;
    private boolean mListening;

    private int mBrightnesSliderVisibility = BRIGHTNESS_SLIDER_SHOW_ON_QS_PANEL;

    public QSContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mQSPanel = (QSPanel) findViewById(R.id.quick_settings_panel);
        mQSDetail = (QSDetail) findViewById(R.id.qs_detail);
        mHeader = (BaseStatusBarHeader) findViewById(R.id.header);
        mQuickQSPanel = (QuickQSPanel) mHeader.findViewById(R.id.quick_qs_panel);
        mQuickQsPanelScrollerContainer =
                (ViewGroup) mHeader.findViewById(R.id.quick_qs_panel_scroll_container);
        mQuickQsPanelScroller = (HorizontalScrollView) mHeader.findViewById(R.id.quick_qs_panel_scroll);
        mQSDetail.setQsPanel(mQSPanel, mHeader);
        mQSAnimator = new QSAnimator(this, mHeader, mQuickQsPanelScroller, mQuickQSPanel,
                mQSPanel);
        mQSCustomizer = (QSCustomizer) findViewById(R.id.qs_customize);
        mQSCustomizer.setQsContainer(this);

        LayoutParams qSPanelLp = (LayoutParams) mQSPanel.getLayoutParams();
        qSPanelLp.topMargin =
                getContext().getResources().getDimensionPixelSize(showBrightnesSliderOnPanel()
                        ? R.dimen.qs_panel_with_brightness_view_margin_top
                        : R.dimen.qs_panel_margin_top);
        mQSPanel.setLayoutParams(qSPanelLp);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        mQSAnimator.onRtlChanged();
    }

    public void setHost(QSTileHost qsh) {
        mQSPanel.setHost(qsh, mQSCustomizer);
        mHeader.setQSPanel(mQSPanel);
        mQSDetail.setHost(qsh);
        mQSAnimator.setHost(qsh);
    }

    public void setPanelView(NotificationPanelView panelView) {
        mPanelView = panelView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Since we control our own bottom, be whatever size we want.
        // Otherwise the QSPanel ends up with 0 height when the window is only the
        // size of the status bar.
        mQSPanel.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.UNSPECIFIED));
        int width = mQSPanel.getMeasuredWidth();
        int height = ((LayoutParams) mQSPanel.getLayoutParams()).topMargin
                + mQSPanel.getMeasuredHeight();
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        // QSCustomizer is always be the height of the screen, but do this after
        // other measuring to avoid changing the height of the QSContainer.
        getDisplay().getRealSize(mSizePoint);
        mQSCustomizer.measure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(mSizePoint.y, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBottom();
    }

    public boolean isCustomizing() {
        return mQSCustomizer.isCustomizing();
    }

    /**
     * Overrides the height of this view (post-layout), so that the content is clipped to that
     * height and the background is set to that height.
     *
     * @param heightOverride the overridden height
     */
    public void setHeightOverride(int heightOverride) {
        mHeightOverride = heightOverride;
        updateBottom();
    }

    /**
     * The height this view wants to be. This is different from {@link #getMeasuredHeight} such that
     * during closing the detail panel, this already returns the smaller height.
     */
    public int getDesiredHeight() {
        if (isCustomizing()) {
            return getHeight();
        }
        if (mQSDetail.isClosingDetail()) {
            int panelHeight = ((LayoutParams) mQSPanel.getLayoutParams()).topMargin
                    + mQSPanel.getMeasuredHeight();
            return panelHeight + getPaddingBottom();
        } else {
            return getMeasuredHeight();
        }
    }

    public void notifyCustomizeChanged() {
        // The customize state changed, so our height changed.
        updateBottom();
        mQSPanel.setVisibility(!mQSCustomizer.isCustomizing() ? View.VISIBLE : View.INVISIBLE);
        mHeader.setVisibility(!mQSCustomizer.isCustomizing() ? View.VISIBLE : View.INVISIBLE);
        // Let the panel know the position changed and it needs to update where notifications
        // and whatnot are.
        mPanelView.onQsHeightChanged();
    }

    private void updateBottom() {
        int height = calculateContainerHeight();
        setBottom(getTop() + height);
        mQSDetail.setBottom(getTop() + height);
    }

    protected int calculateContainerHeight() {
        int heightOverride = mHeightOverride != -1 ? mHeightOverride : getMeasuredHeight();
        return mQSCustomizer.isCustomizing() ? mQSCustomizer.getHeight()
                : (int) (mQsExpansion * (heightOverride - mHeader.getCollapsedHeight()))
                + mHeader.getCollapsedHeight();
    }

    private void updateQsState() {
        boolean expandVisually = mQsExpanded || mStackScrollerOverscrolling || mHeaderAnimating;
        mQSPanel.setExpanded(mQsExpanded);
        mQSDetail.setExpanded(mQsExpanded);
        mHeader.setVisibility((mQsExpanded || !mKeyguardShowing || mHeaderAnimating)
                ? View.VISIBLE
                : View.INVISIBLE);
        mHeader.setExpanded((mKeyguardShowing && !mHeaderAnimating)
                || (mQsExpanded && !mStackScrollerOverscrolling));
        mQSPanel.setVisibility(expandVisually ? View.VISIBLE : View.INVISIBLE);
    }

    public BaseStatusBarHeader getHeader() {
        return mHeader;
    }

    public QSPanel getQsPanel() {
        return mQSPanel;
    }

    public QSCustomizer getCustomizer() {
        return mQSCustomizer;
    }

    public boolean isShowingDetail() {
        return mQSPanel.isShowingCustomize() || mQSDetail.isShowingDetail();
    }

    public void setHeaderClickable(boolean clickable) {
        if (DEBUG) Log.d(TAG, "setHeaderClickable " + clickable);
        mHeader.setClickable(clickable);
    }

    public void setExpanded(boolean expanded) {
        if (DEBUG) Log.d(TAG, "setExpanded " + expanded);
        mQsExpanded = expanded;
        mQSPanel.setListening(mListening && mQsExpanded);
        updateQsState();
    }

    public void setKeyguardShowing(boolean keyguardShowing) {
        if (DEBUG) Log.d(TAG, "setKeyguardShowing " + keyguardShowing);
        mKeyguardShowing = keyguardShowing;
        mQSAnimator.setOnKeyguard(keyguardShowing);
        updateQsState();
    }

    public void setOverscrolling(boolean stackScrollerOverscrolling) {
        if (DEBUG) Log.d(TAG, "setOverscrolling " + stackScrollerOverscrolling);
        mStackScrollerOverscrolling = stackScrollerOverscrolling;
        updateQsState();
    }

    public void setListening(boolean listening) {
        if (DEBUG) Log.d(TAG, "setListening " + listening);
        mListening = listening;
        mHeader.setListening(listening);
        mQSPanel.setListening(mListening && mQsExpanded);
    }

    public void setHeaderListening(boolean listening) {
        mHeader.setListening(listening);
    }

    public void setQsExpansion(float expansion, float headerTranslation) {
        if (DEBUG) Log.d(TAG, "setQSExpansion " + expansion + " " + headerTranslation);
        mQsExpansion = expansion;
        final float translationScaleY = expansion - 1;
        if (!mHeaderAnimating) {
            setTranslationY(mKeyguardShowing ? (translationScaleY * mHeader.getHeight())
                    : headerTranslation);
        }
        mHeader.setExpansion(mKeyguardShowing ? 1 : expansion);
        mQSPanel.setTranslationY(translationScaleY * mQSPanel.getHeight());
        mQSDetail.setFullyExpanded(expansion == 1);
        mQSAnimator.setPosition(expansion);
        updateBottom();

        // Set bounds on the QS panel so it doesn't run over the header.
        mQsBounds.top = (int) (mQSPanel.getHeight() * (1 - expansion));
        mQsBounds.right = mQSPanel.getWidth();
        mQsBounds.bottom = mQSPanel.getHeight();
        mQSPanel.setClipBounds(mQsBounds);
    }

    public void animateHeaderSlidingIn(long delay) {
        if (DEBUG) Log.d(TAG, "animateHeaderSlidingIn");
        // If the QS is already expanded we don't need to slide in the header as it's already
        // visible.
        if (!mQsExpanded) {
            mHeaderAnimating = true;
            mDelay = delay;
            getViewTreeObserver().addOnPreDrawListener(mStartHeaderSlidingIn);
        }
    }

    public void animateHeaderSlidingOut() {
        if (DEBUG) Log.d(TAG, "animateHeaderSlidingOut");
        mHeaderAnimating = true;
        animate().y(-mHeader.getHeight())
                .setStartDelay(0)
                .setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD)
                .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animate().setListener(null);
                        mHeaderAnimating = false;
                        updateQsState();
                    }
                })
                .start();
    }

    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn
            = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);
            animate()
                    .translationY(0f)
                    .setStartDelay(mDelay)
                    .setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE)
                    .setInterpolator(Interpolators.FAST_OUT_SLOW_IN)
                    .setListener(mAnimateHeaderSlidingInListener)
                    .start();
            setY(-mHeader.getHeight());
            return true;
        }
    };

    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mHeaderAnimating = false;
            updateQsState();
        }
    };

    public int getQsMinExpansionHeight() {
        return mHeader.getHeight();
    }

    public void setPrimaryBgColor(ColorStateList color) {
        setBackgroundTintList(color);
        mQSCustomizer.setBackgroundTintList(color);
        ((TransitionDrawable) mQSDetail.getBackground()).findDrawableByLayerId(
                R.id.qs_detail_transition_background).setTintList(color);
        mHeader.updateBrightnessThumbBgColor();
        mQSPanel.updateDndModePanelBgColor();
    }

    public void updateSecondaryBgColor(ColorStateList color) {
        mQSCustomizer.updateDecorationBgColor(color);
    }

    public void updateAccentColor() {
        mQSPanel.updateAccentColor();
        mQSDetail.setAccentColor();
        mHeader.updateAccentColor();
    }

    public void updateTextColor() {
        mQSPanel.updateTextColor();
        mHeader.setTextColor();
        mQuickQSPanel.updateTextColor();
        mQSDetail.setTextColor();
        mQSCustomizer.setActionBarTextColor();
    }

    public void updateIconColor() {
        ((TransitionDrawable) mQSDetail.getBackground()).findDrawableByLayerId(
                R.id.qs_detail_transition_color).setTintList(QSColorHelper.getIconTintList(mContext));
        mQSPanel.updateIconColor();
        mHeader.setIconColor();
        mQSDetail.setIconColor();
        mQuickQSPanel.updateIconColor();
        mQSCustomizer.setActionBarIconColor();
    }

    public void updateRippleColor() {
        mQSPanel.updateRippleColor();
        mHeader.setRippleColor();
        mQSDetail.setRippleColor();
        mQuickQSPanel.updateRippleColor();
        mQSCustomizer.setActionBarRippleColor();
    }

    public void updateBatteryTextColor() {
        mQuickQSPanel.updateBatteryTextColor();
    }

    public void updateBrightnesSliderVisibility(int visibility) {
        mBrightnesSliderVisibility = visibility;
        mHeader.updateBrightnesSliderVisibility(mBrightnesSliderVisibility);

        LayoutParams qSPanelLp = (LayoutParams) mQSPanel.getLayoutParams();
        qSPanelLp.topMargin =
                getContext().getResources().getDimensionPixelSize(showBrightnesSliderOnPanel()
                        ? R.dimen.qs_panel_with_brightness_view_margin_top
                        : R.dimen.qs_panel_margin_top);
        mQSPanel.setLayoutParams(qSPanelLp);
        requestLayout();
    }

    public void updateBatteryMeterType(int type) {
        mQSPanel.updateBatteryMeterType(type);
        mQuickQSPanel.updateBatteryMeterType(type);
    }

    public void updateBatteryMeterTextVisibility(boolean show) {
        mQSPanel.updateBatteryMeterTextVisibility(show);
        mQuickQSPanel.updateBatteryMeterTextVisibility(show);
    }

    public void updateBatteryMeterCircleDots(int interval, int length) {
        mQSPanel.updateBatteryMeterCircleDots(interval, length);
        mQuickQSPanel.updateBatteryMeterCircleDots(interval, length);
    }

    public void updateBatteryMeterCutOutText(boolean cutOutText) {
        mQuickQSPanel.updateBatteryMeterCutOutText(cutOutText);
    }

    public void hideImmediately() {
        animate().cancel();
        setY(-mHeader.getHeight());
    }

    public void updateQSBarEnableScroll(boolean enabled) {
        mQSAnimator.setFancyAnimaton(!enabled);
        mQuickQSPanel.updateQSBarEnableScroll(enabled);
        mQuickQsPanelScrollerContainer.setClipChildren(enabled);
        mQuickQsPanelScrollerContainer.setClipToPadding(enabled);
    }

    public boolean showBrightnesSliderOnBar() {
        return mBrightnesSliderVisibility == BRIGHTNESS_SLIDER_SHOW
                || mBrightnesSliderVisibility == BRIGHTNESS_SLIDER_SHOW_ON_QS_BAR;
    }

    public boolean showBrightnesSliderOnPanel() {
        return mBrightnesSliderVisibility == BRIGHTNESS_SLIDER_SHOW
                || mBrightnesSliderVisibility == BRIGHTNESS_SLIDER_SHOW_ON_QS_PANEL;
    }
}
