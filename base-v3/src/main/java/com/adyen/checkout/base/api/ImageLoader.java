/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 26/7/2019.
 */

package com.adyen.checkout.base.api;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SyntheticAccessor")
public class ImageLoader {
    private static final String TAG = LogUtil.getTag();

    private final LogoApi mLogoApi;
    private final Map<String, LogoConnectionTask.LogoCallback> mCallbacks = new HashMap<>();
    private final Map<String, WeakReference<ImageView>> mImageViews = new HashMap<>();

    @SuppressWarnings("PMD.SingletonClassReturningNewInstance")
    @NonNull
    public static ImageLoader getInstance(@NonNull Context context, @NonNull Environment environment) {
        return new ImageLoader(LogoApi.getInstance(environment, context.getResources().getDisplayMetrics()));
    }

    /**
     * Loading Image from LogoApi.
     */
    public ImageLoader(@NonNull LogoApi logoApi) {
        this.mLogoApi = logoApi;
    }

    /**
     * Load image to ImageView.
     */
    public void load(@NonNull String txVariant, @NonNull ImageView view) {
        this.load(txVariant, view, 0, 0);
    }

    /**
     * Load image to ImageView.
     */
    public void load(@NonNull String txVariant, @Nullable String txSubVariant, @NonNull ImageView view) {
        this.load(txVariant, txSubVariant, view, 0, 0);
    }

    /**
     * Load image to ImageView with place holder before load and error fallback image.
     */
    public void load(@NonNull String txVariant, @NonNull ImageView view, @Nullable @DrawableRes int placeholder,
            @Nullable @DrawableRes final int errorFallback) {
        this.load(txVariant, "", view, placeholder, errorFallback);
    }

    /**
     * Load image to ImageView with place holder before load and error fallback image.
     */
    public void load(@NonNull String txVariant, @NonNull String txSubVariant, @NonNull ImageView view, @Nullable @DrawableRes int placeholder,
            @Nullable @DrawableRes final int errorFallback) {

        if (placeholder != 0) {
            view.setImageResource(placeholder);
        }

        final String id = txVariant + txSubVariant + view.getId();

        if (mCallbacks.containsKey(id)) {
            mCallbacks.remove(id);
            mImageViews.remove(id);
        }

        final LogoConnectionTask.LogoCallback callback = new LogoConnectionTask.LogoCallback() {
            @Override
            public void onLogoReceived(@NonNull BitmapDrawable drawable) {
                if (mImageViews.containsKey(id)) {
                    final ImageView imageView = mImageViews.get(id).get();
                    if (imageView != null) {
                        imageView.setImageDrawable(drawable);
                    } else {
                        Logger.e(TAG, "ImageView is null for received Logo - " + id);
                    }

                    mCallbacks.remove(id);
                    mImageViews.remove(id);
                }
            }

            @Override
            public void onReceiveFailed() {
                final ImageView imageView = mImageViews.get(id).get();
                if (imageView != null) {
                    imageView.setImageResource(errorFallback);
                }

                mCallbacks.remove(id);
                mImageViews.remove(id);
            }
        };

        mImageViews.put(id, new WeakReference<>(view));
        mCallbacks.put(id, callback);
        mLogoApi.getLogo(txVariant, txSubVariant, null, callback);
    }
}
