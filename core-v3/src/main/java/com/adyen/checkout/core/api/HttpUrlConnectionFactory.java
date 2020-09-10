/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 26/01/2018.
 */

package com.adyen.checkout.core.api;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.net.HttpURLConnection;

/**
 * A factory that creates a URL connections using a secure socket encryption.
 */
final class HttpUrlConnectionFactory extends BaseHttpUrlConnectionFactory {
    private static final String TAG = LogUtil.getTag();

    private static final String ERROR_MESSAGE_INSECURE_CONNECTION = "Trying to connect to a URL that is not HTTPS.";

    private static HttpUrlConnectionFactory sInstance;

    /**
     * Get the instance of the {@link HttpUrlConnectionFactory}.
     *
     * @return The instance of the {@link HttpUrlConnectionFactory}.
     */
    @NonNull
    static HttpUrlConnectionFactory getInstance() {
        synchronized (HttpUrlConnectionFactory.class) {
            if (sInstance == null) {
                sInstance = new HttpUrlConnectionFactory();
            }
            return sInstance;
        }
    }

    private HttpUrlConnectionFactory() {
        // Private constructor for Singleton
    }

    @NonNull
    @Override
    HttpURLConnection handleInsecureConnection(@NonNull HttpURLConnection httpUrlConnection) {
        Logger.w(TAG, ERROR_MESSAGE_INSECURE_CONNECTION);
        return httpUrlConnection;
    }
}
