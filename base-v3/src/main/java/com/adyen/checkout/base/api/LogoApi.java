/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/3/2019.
 */

package com.adyen.checkout.base.api;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LogoApi {
    private static final String TAG = LogUtil.getTag();


    //%1$s = size, %2$s = txVariant(/txSubVariant)-densityExtension
    private static final String LOGO_PATH = "images/logos/%1$s/%2$s.png";
    private static final Size DEFAULT_SIZE = Size.SMALL;

    @SuppressWarnings(Lint.SYNTHETIC)
    static final int KILO_BYTE_SIZE = 1024;
    private static final int LRU_CACHE_MAX_SIZE;

    static {
        final int availableMemory = (int) (Runtime.getRuntime().maxMemory() / KILO_BYTE_SIZE);
        final int cacheFractionSize = 8;
        LRU_CACHE_MAX_SIZE = availableMemory / cacheFractionSize;
    }

    private static LogoApi sInstance;

    private final String mLogoUrlFormat;
    private final String mDensityExtension;
    private final LruCache<String, BitmapDrawable> mCache;

    private final Map<String, LogoConnectionTask> mConnectionsMap = new HashMap<>();

    /**
     * Get the instance of the {@link LogoApi} for the specified environment.
     *
     * @param environment The URL of the server for fetching the images. For optimization it should be the closest to the shopper.
     * @param displayMetrics The {@link DisplayMetrics} of the device to fetch the correct size images.
     * @return The instance of the {@link LogoApi}.
     */
    @NonNull
    public static LogoApi getInstance(@NonNull Environment environment, @NonNull DisplayMetrics displayMetrics) {
        final String hostUrl = environment.getBaseUrl();
        synchronized (LogoApi.class) {
            if (sInstance == null || isDifferentHost(sInstance, hostUrl)) {
                LogoApi.clearCache(sInstance);
                sInstance = new LogoApi(hostUrl, displayMetrics);
            }
            return sInstance;
        }
    }

    /**
     * This method can be called if there is a need to release memory usage.
     *
     * @param instance The instance of the Api that will have it's cache cleared.
     */
    @SuppressWarnings(Lint.MERCHANT_VISIBLE)
    public static void clearCache(@Nullable LogoApi instance) {
        if (instance != null) {
            instance.mCache.evictAll();
        }
    }

    private static boolean isDifferentHost(@NonNull LogoApi logoApi, @NonNull String hostUrl) {
        return !logoApi.mLogoUrlFormat.startsWith(hostUrl);
    }

    private LogoApi(@NonNull String host, @NonNull DisplayMetrics displayMetrics) {
        Logger.v(TAG, "Environment URL - " + host);
        mLogoUrlFormat = host + LOGO_PATH;
        mDensityExtension = getDensityExtension(displayMetrics.densityDpi);
        mCache = new LruCache<String, BitmapDrawable>(LRU_CACHE_MAX_SIZE) {
            @Override
            protected int sizeOf(String key, BitmapDrawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items.
                return drawable.getBitmap().getByteCount() / KILO_BYTE_SIZE;
            }
        };
    }

    /**
     * Starts a request to get the {@link Drawable} of a Logo from the web.
     *
     * @param txVariant The identifier of the transaction variant.
     * @param txSubVariant The identifier of the transaction sub variant.
     * @param size The size if the desired logo;
     * @param callback The callback for when the request is completed.
     */
    public void getLogo(@NonNull String txVariant, @Nullable String txSubVariant, @Nullable Size size,
            @NonNull LogoConnectionTask.LogoCallback callback) {
        Logger.v(TAG, "getLogo - " + txVariant + ", " + txSubVariant + ", " + size);
        final String logoUrl = buildUrl(txVariant, txSubVariant, size);

        synchronized (this) {
            final BitmapDrawable cachedLogo = mCache.get(logoUrl);
            if (cachedLogo != null) {
                Logger.v(TAG, "returning cached logo");
                callback.onLogoReceived(cachedLogo);
            } else if (!mConnectionsMap.containsKey(logoUrl)) {
                final LogoConnectionTask logoConnectionTask = new LogoConnectionTask(this, logoUrl, callback);
                mConnectionsMap.put(logoUrl, logoConnectionTask);
                ThreadManager.EXECUTOR.submit(logoConnectionTask);
            } else {
                Logger.d(TAG,
                        "Execution for " + txVariant + (TextUtils.isEmpty(txSubVariant) ? "" : "/" + txSubVariant) + " is already running.");
            }
        }
    }

    /**
     * Cancels a specific request based on the previously sent parameters.
     * If a previous callback exists, it will be triggered as receive failed.
     *
     * @param txVariant The identifier of the transaction variant.
     * @param txSubVariant The identifier of the transaction sub variant.
     * @param size The size if the desired logo;
     */
    public void cancelLogoRequest(@NonNull String txVariant, @Nullable String txSubVariant, @Nullable Size size) {
        Logger.d(TAG, "cancelLogoRequest");
        final String logoUrl = buildUrl(txVariant, txSubVariant, size);

        synchronized (this) {
            final LogoConnectionTask taskToCancel = mConnectionsMap.remove(logoUrl);
            if (taskToCancel != null) {
                taskToCancel.cancel(true);
                Logger.d(TAG, "canceled");
            }
        }
    }

    /**
     * Cancels all current requests.
     */
    public void cancellAll() {
        synchronized (this) {
            for (LogoConnectionTask task : mConnectionsMap.values()) {
                task.cancel(true);
            }
            mConnectionsMap.clear();
        }
    }

    void taskFinished(@NonNull String logoUrl, @Nullable BitmapDrawable logo) {
        synchronized (this) {
            mConnectionsMap.remove(logoUrl);
            if (logo != null) {
                mCache.put(logoUrl, logo);
            }
        }
    }

    @NonNull
    private String getDensityExtension(int densityDpi) {
        if (densityDpi <= DisplayMetrics.DENSITY_LOW) {
            return "-ldpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_MEDIUM) {
            // no extension
            return "";
        } else if (densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            return "-hdpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_XHIGH) {
            return "-xhdpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_XXHIGH) {
            return "-xxhdpi";
        } else {
            return "-xxxhdpi";
        }
    }

    @NonNull
    private String buildUrl(@NonNull String txVariant, @Nullable String txSubVariant, @Nullable Size size) {
        if (txSubVariant != null && !txSubVariant.isEmpty())  {
            return String.format(mLogoUrlFormat, getSizeVariant(size), txVariant + "/" + txSubVariant + mDensityExtension);
        } else {
            return String.format(mLogoUrlFormat, getSizeVariant(size), txVariant + mDensityExtension);
        }
    }

    @NonNull
    private String getSizeVariant(@Nullable Size size) {
        if (size == null) {
            return DEFAULT_SIZE.toString();
        } else {
            return size.toString();
        }
    }

    /**
     * The logo size.
     */
    public enum Size {
        /**
         * Size for small logos (height: 26dp).
         */
        SMALL,
        /**
         * Size for medium logos (height: 50dp).
         */
        MEDIUM,
        /**
         * Size for large logos (height: 100dp).
         */
        LARGE;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
