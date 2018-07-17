package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;
import android.util.Log;

import java.net.HttpURLConnection;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 26/01/2018.
 */
class HttpUrlConnectionFactory extends BaseHttpUrlConnectionFactory {
    private static final String TAG = HttpUrlConnectionFactory.class.getSimpleName();

    @NonNull
    @Override
    HttpURLConnection handleInsecureConnection(@NonNull HttpURLConnection httpUrlConnection) {
        Log.w(TAG, ERROR_MESSAGE_INSECURE_CONNECTION);

        return httpUrlConnection;
    }
}
