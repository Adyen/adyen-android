/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/12/2017.
 */

package com.adyen.checkout.base.internal;

import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.adyen.checkout.base.HostProvider;
import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.base.TxSubVariantProvider;
import com.adyen.checkout.base.TxVariantProvider;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Callable;

public final class LogoApiImpl extends LogoApi implements ComponentCallbacks {
    //%1$s = size, %2$s = txVariant(/txSubVariant)-densityExtension
    private static final String LOGO_PATH = "checkoutshopper/images/logos/%1$s/%2$s.png";

    private static final int LRU_CACHE_MAX_SIZE = 50;

    private static final Size DEFAULT_SIZE = Size.SMALL;

    private static LogoApiImpl sInstance;

    private final Application mApplication;

    private final String mLogoUrlFormat;

    private final String mDensityExtension;

    private final LruCache<String, Drawable> mCache;

    @NonNull
    public static synchronized LogoApiImpl getInstance(@NonNull Application application, @NonNull HostProvider hostProvider) {
        if (sInstance == null || isDifferentHost(sInstance, hostProvider)) {
            destroyInstance();
            sInstance = new LogoApiImpl(application, hostProvider);
            application.registerComponentCallbacks(sInstance);
        }

        return sInstance;
    }

    private static boolean isDifferentHost(@NonNull LogoApiImpl logoApi, @NonNull HostProvider hostProvider) {
        return !logoApi.mLogoUrlFormat.startsWith(hostProvider.getUrl());
    }

    private static synchronized void destroyInstance() {
        if (sInstance != null) {
            sInstance.mApplication.unregisterComponentCallbacks(sInstance);
            sInstance = null;
        }
    }

    private LogoApiImpl(@NonNull Application application, @NonNull HostProvider hostProvider) {
        mApplication = application;
        mLogoUrlFormat = hostProvider.getUrl() + LOGO_PATH;
        mDensityExtension = getDensityExtension(application.getResources().getDisplayMetrics().densityDpi);
        mCache = new LruCache<>(LRU_CACHE_MAX_SIZE);
    }

    @NonNull
    @Override
    public Builder newBuilder(@NonNull TxVariantProvider txVariantProvider) {
        return new BuilderImpl(txVariantProvider);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String newDensityExtension = getDensityExtension(newConfig.densityDpi);

            if (!mDensityExtension.equals(newDensityExtension)) {
                destroyInstance();
            }
        }
    }

    @Override
    public void onLowMemory() {
        mCache.evictAll();
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

    private final class BuilderImpl implements Builder {
        private final TxVariantProvider mTxVariantProvider;

        private TxSubVariantProvider mTxSubVariantProvider;

        private Size mSize;

        private BuilderImpl(@NonNull TxVariantProvider txVariantProvider) {
            mTxVariantProvider = txVariantProvider;
        }

        @NonNull
        @Override
        public Builder setTxSubVariantProvider(@Nullable TxSubVariantProvider txSubVariantProvider) {
            mTxSubVariantProvider = txSubVariantProvider;

            return this;
        }

        @NonNull
        @Override
        public Builder setSize(@Nullable Size size) {
            mSize = size;

            return this;
        }

        @NonNull
        @Override
        public String buildUrl() {
            String txVariant = mTxVariantProvider.getTxVariant();

            if (mTxSubVariantProvider != null) {
                String txSubVariant = mTxSubVariantProvider.getTxSubVariant();

                return String.format(mLogoUrlFormat, getSizeVariant(), txVariant + "/" + txSubVariant + mDensityExtension);
            } else {
                return String.format(mLogoUrlFormat, getSizeVariant(), txVariant + mDensityExtension);
            }
        }

        @NonNull
        @Override
        public Callable<Drawable> buildCallable() {
            final String logoUrl = buildUrl();

            return new Callable<Drawable>() {
                @Override
                public Drawable call() throws Exception {
                    Drawable drawable = mCache.get(logoUrl);

                    if (drawable == null) {
                        byte[] bytes = get(logoUrl, Collections.<String, String>emptyMap());
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        drawable = new BitmapDrawable(Resources.getSystem(), bitmap);
                        mCache.put(logoUrl, drawable);
                    }

                    return drawable;
                }
            };
        }

        @NonNull
        private String getSizeVariant() {
            Size size = mSize != null ? mSize : DEFAULT_SIZE;

            return size.name().toLowerCase(Locale.US);
        }
    }
}
