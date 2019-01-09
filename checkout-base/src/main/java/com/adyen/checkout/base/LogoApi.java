/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/12/2017.
 */

package com.adyen.checkout.base;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Api;
import com.adyen.checkout.base.internal.LogoApiImpl;

import java.util.concurrent.Callable;

/**
 * The {@link LogoApi} class provides means to generate URLs for logos and to create {@link Callable} objects with which {@link Drawable} objects
 * can be retrieved directly.
 */
public abstract class LogoApi extends Api {
    /**
     * The {@link HostProvider} connecting to the test environment.
     */
    @NonNull
    public static final HostProvider TEST = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://checkoutshopper-test.adyen.com/";
        }
    };

    /**
     * The {@link HostProvider} connecting to the live environment in Europe.
     */
    @NonNull
    public static final HostProvider LIVE_EU = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://checkoutshopper-live.adyen.com/";
        }
    };

    /**
     * The {@link HostProvider} connecting to the live environment in the US.
     */
    @NonNull
    public static final HostProvider LIVE_US = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://checkoutshopper-live-us.adyen.com/";
        }
    };

    /**
     * The {@link HostProvider} connecting to the live environment in Australia.
     */
    @NonNull
    public static final HostProvider LIVE_AU = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://checkoutshopper-live-au.adyen.com/";
        }
    };

    /**
     * Get the {@link LogoApi} instance.
     *
     * @param application The current {@link Application}.
     * @param hostProvider The {@link HostProvider} indicating the host to connect to.
     * @return The {@link LogoApi} instance.
     */
    @NonNull
    public static LogoApi getInstance(@NonNull Application application, @NonNull HostProvider hostProvider) {
        return LogoApiImpl.getInstance(application, hostProvider);
    }

    /**
     * Creates a new {@link Builder}.
     *
     * @param txVariantProvider A {@link TxVariantProvider} to retrieve the logo for.
     * @return A new {@link Builder}.
     */
    @NonNull
    public abstract Builder newBuilder(@NonNull TxVariantProvider txVariantProvider);

    /**
     * Builder class for retrieving a logo.
     */
    public interface Builder {
        /**
         * Set the {@link TxSubVariantProvider} of the {@link TxVariantProvider} to retrieve the logo for.
         *
         * @param txSubVariantProvider A {@link TxSubVariantProvider} of the {@link TxVariantProvider}.
         * @return This {@link Builder} object.
         */
        @NonNull
        Builder setTxSubVariantProvider(@Nullable TxSubVariantProvider txSubVariantProvider);

        /**
         * Set the {@link Size} of the logo to retrieve.
         *
         * @param size The {@link Size} of the logo to retrieve. Defaults to {@link Size#SMALL}.
         * @return This {@link Builder} object.
         */
        @NonNull
        Builder setSize(@Nullable Size size);

        /**
         * Builds the logo URL.
         *
         * @return The logo URL.
         */
        @NonNull
        String buildUrl();

        /**
         * Builds a {@link Callable}.
         *
         * @return The {@link Callable}.
         */
        @NonNull
        Callable<Drawable> buildCallable();
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
        LARGE
    }
}
