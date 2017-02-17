/*
 * Copyright (C) 2017 DarkKat
 *
 * Based on RecentExpandedCard by SlimRoms Project
 * Copyright (C) 2014-2017 SlimRoms Project
 * Author: Lars Greiss - email: kufikugel@googlemail.com
 * https://github.com/SlimRoms/frameworks_opt_slim/blob/ng7.1/packages/SlimSystemUI/src/com/android/systemui/slimrecent/RecentExpandedCard.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.systemui.slimrecent;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import com.android.systemui.R;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * This class handles async screenshots load for the requested apps
 * and put them when sucessfull into the LRU cache.
 */
public class ThumbnailLoader {

    /**
     * Singleton.
     */
    private static ThumbnailLoader sInstance;

    private Context mContext;

    /**
     * Get the instance.
     */
    public static ThumbnailLoader getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        } else {
            return sInstance = new ThumbnailLoader(context);
        }
    }

    /**
     * Constructor.
     */
    private ThumbnailLoader(Context context) {
        mContext = context;
    }

    /**
     * Load the app screenshot via async task.
     *
     * @params persistentTaskId
     * @params imageView
     * @params aspectRatio
     */
    protected void loadThumbnail(int persistentTaskId, RecentImageView imageView, float aspectRatio) {
        final BitmapDownloaderTask task =
                new BitmapDownloaderTask(imageView, mContext, aspectRatio);
        task.executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, persistentTaskId);
    }

    final static BitmapFactory.Options sBitmapOptions;

    static {
        sBitmapOptions = new BitmapFactory.Options();
        sBitmapOptions.inMutable = true;
    }


    // Loads the actual task bitmap.
    private static Bitmap loadBitmap(int persistentTaskId, Context context, float aspectRatio) {
        if (context == null) {
            return null;
        }
        final ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        return getResizedBitmap(getThumbnail(am, persistentTaskId), context, aspectRatio);
    }

    /**
     * Returns a task thumbnail from the activity manager
     */
    public static Bitmap getThumbnail(ActivityManager activityManager, int taskId) {
        ActivityManager.TaskThumbnail taskThumbnail = activityManager.getTaskThumbnail(taskId);
        if (taskThumbnail == null) return null;

        Bitmap thumbnail = taskThumbnail.mainThumbnail;
        ParcelFileDescriptor descriptor = taskThumbnail.thumbnailFileDescriptor;
        if (thumbnail == null && descriptor != null) {
            thumbnail = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(),
                    null, sBitmapOptions);
        }
        if (descriptor != null) {
            try {
                descriptor.close();
            } catch (IOException e) {
            }
        }
        return thumbnail;
    }

    // Resize and crop the task bitmap to the overlay values.
    private static Bitmap getResizedBitmap(Bitmap source, Context context, float aspectRatio) {
        if (source == null) {
            return null;
        }

        final Resources res = context.getResources();
        final int thumbnailWidth =
                res.getDimensionPixelSize(R.dimen.recent_card_view_thumbnail_width);
        final int thumbnailHeight = Math.round(thumbnailWidth * aspectRatio);

        final int sourceWidth = source.getWidth();
        final int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        final float xScale = (float) thumbnailWidth / sourceWidth;
        final float yScale = (float) thumbnailHeight / sourceHeight;
        final float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        final float scaledWidth = scale * sourceWidth;
        final float scaledHeight = scale * sourceHeight;

        // Let's find out the left coordinates if the scaled bitmap
        // should be centered in the new size given by the parameters
        final float left = (thumbnailWidth - scaledWidth) / 2;

        // The target rectangle for the new, scaled version of the source bitmap
        final RectF targetRect = new RectF(left, 0.0f, left + scaledWidth, scaledHeight);

        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        final Bitmap dest = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Config.ARGB_8888);
        final Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, paint);

        return dest;
    }

    // AsyncTask loader for the task bitmap.
    private static class BitmapDownloaderTask extends AsyncTask<Integer, Void, Bitmap> {

        private boolean mLoaded;

        private final WeakReference<RecentImageView> rImageViewReference;
        private final WeakReference<Context> rContext;

        private int mOrigPri;

        private String mLRUCacheKey;
        private float mAspectRatio;

        public BitmapDownloaderTask(RecentImageView imageView,
                Context context, float aspectRatio) {
            rImageViewReference = new WeakReference<RecentImageView>(imageView);
            rContext = new WeakReference<Context>(context);
            mAspectRatio = aspectRatio;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            mLoaded = false;
            mLRUCacheKey = null;
            // Save current thread priority and set it during the loading
            // to background priority.
            mOrigPri = Process.getThreadPriority(Process.myTid());
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (isCancelled() || rContext == null) {
                return null;
            }
            mLRUCacheKey = String.valueOf(params[0]);
            // Load and return bitmap
            return loadBitmap(params[0], rContext.get(), mAspectRatio);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            // Restore original thread priority.
            Process.setThreadPriority(mOrigPri);

            // Assign image to the view.
            if (rImageViewReference != null) {
                mLoaded = true;
                final RecentImageView imageView = rImageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    if (bitmap != null && rContext != null) {
                        final Context context = rContext.get();
                        if (context != null) {
                            // Put the loaded bitmap into the LRU cache for later use.
                            CacheController.getInstance(context)
                                    .addBitmapToMemoryCache(mLRUCacheKey, bitmap);
                        }
                    }
                }
            }
        }
    }
}
