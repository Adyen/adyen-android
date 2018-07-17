package com.adyen.checkout.core.card;

import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.HostProvider;
import com.adyen.checkout.base.internal.Api;
import com.adyen.checkout.core.card.internal.CardApiImpl;

import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 25/01/2018.
 */
public abstract class CardApi extends Api {
    /**
     * The {@link HostProvider} connecting to the test environment.
     */
    public static final HostProvider TEST = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://test.adyen.com/";
        }
    };

    /**
     * The {@link HostProvider} connecting to the live environment in Europe.
     */
    public static final HostProvider LIVE_EU = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://live.adyen.com/";
        }
    };

    /**
     * The {@link HostProvider} connecting to the live environment in the US.
     */
    public static final HostProvider LIVE_US = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://live-us.adyen.com/";
        }
    };

    /**
     * The {@link HostProvider} connecting to the live environment in Australia.
     */
    public static final HostProvider LIVE_AU = new HostProvider() {
        @NonNull
        @Override
        public String getUrl() {
            return "https://live-au.adyen.com/";
        }
    };

    /**
     * Get the {@link CardApi} instance.
     *
     * @param application The current {@link Application}.
     * @param hostProvider The {@link HostProvider} indicating the host to connect to.
     * @return The {@link CardApi} instance.
     */
    @NonNull
    public static CardApi getInstance(@NonNull Application application, @NonNull HostProvider hostProvider) {
        return CardApiImpl.getInstance(application, hostProvider);
    }

    /**
     * Creates a {@link Callable} object to retrieve a public key for a given public key token.
     *
     * @param publicKeyToken The public key token.
     * @return A {@link Callable} object to retrieve a public key for a given public key token.
     */
    @NonNull
    public abstract Callable<String> getPublicKey(@NonNull String publicKeyToken);
}
