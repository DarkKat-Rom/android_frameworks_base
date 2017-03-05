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

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.UserHandle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.util.ArrayList;
import java.util.List;

public class RecentRecyclerViewAdapter extends RecyclerView.Adapter<RecentRecyclerViewAdapter.ViewHolder> {
    private static final String PLAYSTORE_REFERENCE = "com.android.vending";
    private static final String AMAZON_REFERENCE    = "com.amazon.venezia";

    private static final String PLAYSTORE_APP_URI_QUERY = "market://details?id=";
    private static final String AMAZON_APP_URI_QUERY    = "amzn://apps/android?p=";

    private static final float THUMBNAIL_ASPECT_RATIO_1_1  = 1f;
    private static final float THUMBNAIL_ASPECT_RATIO_5_4  = 4f / 5f;
    private static final float THUMBNAIL_ASPECT_RATIO_4_3  = 3f / 4f;
    private static final float THUMBNAIL_ASPECT_RATIO_16_9 = 9f / 16f;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<RecentCard> mCards;

    private float mThumbnailAspectRatio;

    private OnCardClickListener mOnCardClickListener;
    private OnAppIconLongClickListener mOnAppIconLongClickListener;
    private OnCardExpandListener mOnCardExpandListener;

    public interface OnCardClickListener {
        public void onClick(RecentCard card);
    }

    public interface OnAppIconLongClickListener {
        public boolean onLongClick(RecentCard card);
    }

    public interface OnCardExpandListener {
        public void onCardExpand(TaskDescription td, boolean expanded);
    }

    public RecentRecyclerViewAdapter(Context context, List<RecentCard> cards) {
        super();

        mContext = context;
        if (cards != null) {
            mCards = cards;
        } else {
            mCards = new ArrayList<RecentCard>();
        }
        mThumbnailAspectRatio = getThumbnailAspectRatio();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public View mAppIconLayout;
        public RecentImageView mAppIcon;
        public RecentImageView mFavoriteIcon;
        public TextView mAppTitle;
        public RecentImageView mThumbnail;
        public View mActionDivider;
        public ImageView mActionInfo;
        public ImageView mActionMultiWindow;
        public ImageView mActionShop;
        public ImageView mActionExpand;
        public ImageView mActionScreenPinning;

        public ViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.recent_card_view);
            mAppIconLayout = v.findViewById(R.id.recent_card_view_header_app_icon_layout);
            mAppIcon = (RecentImageView) v.findViewById(R.id.recent_card_view_header_app_icon);
            mFavoriteIcon = (RecentImageView) v.findViewById(R.id.recent_card_view_header_favorite_icon);
            mAppTitle = (TextView) v.findViewById(R.id.recent_card_view_header_app_title);
            mThumbnail = (RecentImageView) v.findViewById(R.id.recent_card_view_thumbnail);
            mActionDivider = v.findViewById(R.id.recent_card_view_action_divider);
            mActionInfo = (ImageView) v.findViewById(R.id.recent_card_view_action_info);
            mActionMultiWindow = (ImageView) v.findViewById(R.id.recent_card_view_action_multi_window);
            mActionShop = (ImageView) v.findViewById(R.id.recent_card_view_action_shop);
            mActionExpand = (ImageView) v.findViewById(R.id.recent_card_view_action_expand);
            mActionScreenPinning = (ImageView) v.findViewById(R.id.recent_card_view_action_screen_pinning);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public RecentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(
                R.layout.recent_card_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecentCard card = mCards.get(position);
        TaskDescription td = card.getTaskDescription();

        boolean isTopTask = (td.getExpandedState()
                & RecentPanelView.EXPANDED_STATE_TOPTASK) != 0;
        final boolean isSystemExpanded =
                (td.getExpandedState() & RecentPanelView.EXPANDED_STATE_BY_SYSTEM) != 0;
        final boolean isUserExpanded =
                (td.getExpandedState() & RecentPanelView.EXPANDED_STATE_EXPANDED) != 0;
        final boolean isUserCollapsed =
                (td.getExpandedState() & RecentPanelView.EXPANDED_STATE_COLLAPSED) != 0;
        final boolean isExpanded =
                ((isSystemExpanded && !isUserCollapsed) || isUserExpanded) && !isTopTask;

        boolean showFavorite = card.isFavorite();
        boolean showShop = checkAppInstaller(td.packageName, AMAZON_REFERENCE)
                || checkAppInstaller(td.packageName, PLAYSTORE_REFERENCE);
        boolean showExpand = !isTopTask;
        boolean showScreenPinning = isTopTask && screenPinningEnabled();


        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCardClickListener != null) {
                    mOnCardClickListener.onClick(card);
                }
            }
        });
        holder.mAppIconLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnAppIconLongClickListener != null) {
                    return mOnAppIconLongClickListener.onLongClick(card);
                }
                return false;
            }
        });

        holder.mActionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getAppInfoIntent(td);
                RecentController.sendCloseSystemWindows("close_recents");
                intent.setComponent(intent.resolveActivity(mContext.getPackageManager()));
                TaskStackBuilder.create(mContext)
                        .addNextIntentWithParentStack(intent).startActivities(
                                RecentPanelView.getAnimation(mContext, getRecentGravity()));
            }
        });

        holder.mActionMultiWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptions options = ActivityOptions.makeBasic();
                options.setDockCreateMode(0);
                options.setLaunchStackId(ActivityManager.StackId.DOCKED_STACK_ID);
                try {
                    ActivityManagerNative.getDefault()
                            .startActivityFromRecents(card.getPersistentTaskId(), options.toBundle());
                } catch (RemoteException e) {
                }
            }
        });

        if (showShop) {
            holder.mActionShop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getStoreIntent(td);
                    RecentController.sendCloseSystemWindows("close_recents");
                    intent.setComponent(intent.resolveActivity(mContext.getPackageManager()));
                    TaskStackBuilder.create(mContext)
                            .addNextIntentWithParentStack(intent).startActivities(
                                    RecentPanelView.getAnimation(mContext, getRecentGravity()));
                }
            });
        } else {
            holder.mActionShop.setOnClickListener(null);
        }

        if (showExpand) {
            holder.mActionExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnCardExpandListener != null) {
                        mOnCardExpandListener.onCardExpand(td, isExpanded);
                        TransitionManager.beginDelayedTransition(mRecyclerView);
                    }
                }
            });
        } else {
            holder.mActionExpand.setOnClickListener(null);
        }

        if (showScreenPinning) {
            holder.mActionScreenPinning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context appContext = mContext.getApplicationContext();
                    if (appContext == null) {
                        appContext = mContext;
                    }
                    if (appContext instanceof SystemUIApplication) {
                        SystemUIApplication app = (SystemUIApplication) appContext;
                        PhoneStatusBar statusBar = app.getComponent(PhoneStatusBar.class);
                        if (statusBar != null) {
                            statusBar.showScreenPinningRequest(card.getPersistentTaskId(), false);
                        }
                    }
                }
            });
        } else {
            holder.mActionScreenPinning.setOnClickListener(null);
        }

        card.loadAppicon(holder.mAppIcon);
        card.loadThumbnail(holder.mThumbnail, mThumbnailAspectRatio);
        holder.mActionExpand.setImageResource(isExpanded
                ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        holder.mAppTitle.setText(card.getLabel());

        holder.mCardView.setCardBackgroundColor(card.getBackgroundColor());
        holder.mAppTitle.setTextColor(card.getHeaderTextColor());
        holder.mThumbnail.setBackgroundTintList(card.getThumbnailFrameTint());
        holder.mActionDivider.setBackgroundTintList(card.getDividerTint());
        holder.mActionInfo.setImageTintList(card.getActionIconTint());
        holder.mActionShop.setImageTintList(card.getActionIconTint());
        holder.mActionMultiWindow.setImageTintList(card.getActionIconTint());
        holder.mActionExpand.setImageTintList(card.getActionIconTint());
        holder.mActionScreenPinning.setImageTintList(card.getActionIconTint());
        setActionRippleColor(holder.mActionInfo, card);
        setActionRippleColor(holder.mActionShop, card);
        setActionRippleColor(holder.mActionMultiWindow, card);
        setActionRippleColor(holder.mActionExpand, card);
        setActionRippleColor(holder.mActionScreenPinning, card);

        holder.mFavoriteIcon.setVisibility(showFavorite ? View.VISIBLE : View.INVISIBLE);
        holder.mThumbnail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.mActionDivider.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.mActionShop.setVisibility(showShop ? View.VISIBLE : View.INVISIBLE);
        holder.mActionExpand.setVisibility(showExpand ? View.VISIBLE : View.GONE);
        holder.mActionScreenPinning.setVisibility(showScreenPinning ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public float getThumbnailAspectRatio() {
        float aspectRatio = THUMBNAIL_ASPECT_RATIO_1_1;
        int value = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SLIM_RECENTS_THUMBNAIL_ASPECT_RATIO, 0);

        switch (value) {
            case 1:
                aspectRatio = THUMBNAIL_ASPECT_RATIO_5_4;
                break;
            case 2:
                aspectRatio = THUMBNAIL_ASPECT_RATIO_4_3;
                break;
            case 3:
                aspectRatio = THUMBNAIL_ASPECT_RATIO_16_9;
                break;
            default:
                break;
        }
        return aspectRatio;
    }

    public void setThumbnailAspectRatio() {
        if (mThumbnailAspectRatio != getThumbnailAspectRatio()) {
            mThumbnailAspectRatio = getThumbnailAspectRatio();
            for (RecentCard card : mCards) {
                card.setForceThumbnailUpdate(true);
            }
        }
    }

    public void setOnCardClickListener(OnCardClickListener onClickListener) {
        mOnCardClickListener = onClickListener;
    }

    public void setOnAppIconLongClickListener(OnAppIconLongClickListener onLongClickListener) {
        mOnAppIconLongClickListener = onLongClickListener;
    }

    public void setOnCardExpandListener(OnCardExpandListener onCardExpandListener) {
        mOnCardExpandListener = onCardExpandListener;
    }

    public void setActionRippleColor(ImageView iv, RecentCard card) {
        if (iv != null && iv.getBackground() instanceof RippleDrawable) {
            ((RippleDrawable) iv.getBackground()).setColor(card.getActionRippleColor());
        }
    }

    private boolean screenPinningEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LOCK_TO_APP_ENABLED, 0) == 1;
    }

    private Intent getAppInfoIntent(TaskDescription td) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", td.packageName, null));
    }

    private Intent getStoreIntent(TaskDescription td) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String reference;
        if (checkAppInstaller(td.packageName, AMAZON_REFERENCE)) {
            reference = AMAZON_REFERENCE;
            intent.setData(Uri.parse(AMAZON_APP_URI_QUERY + td.packageName));
        } else {
            reference = PLAYSTORE_REFERENCE;
            intent.setData(Uri.parse(PLAYSTORE_APP_URI_QUERY + td.packageName));
        }
        // Exclude from recents if the store is not in our task list.
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    /**
     * Check if the requested app was installed by the reference store.
     */
    private boolean checkAppInstaller(String packageName, String reference) {
        if (packageName == null) {
            return false;
        }
        PackageManager pm = mContext.getPackageManager();
        if (!isReferenceInstalled(reference, pm)) {
            return false;
        }

        String installer = pm.getInstallerPackageName(packageName);
        if (reference.equals(installer)) {
            return true;
        }
        return false;
    }

    /**
     * Check is store reference is installed.
     */
    private boolean isReferenceInstalled(String packagename, PackageManager pm) {
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private int getRecentGravity() {
        // Get user gravity.
        int userGravity = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.SLIM_RECENTS_PANEL_GRAVITY, Gravity.RIGHT,
                UserHandle.USER_CURRENT);
        if (mContext.getResources()
                .getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (userGravity == Gravity.LEFT) {
                return Gravity.RIGHT;
            } else {
                return Gravity.LEFT;
            }
        } else {
            return userGravity;
        }
    }
}
