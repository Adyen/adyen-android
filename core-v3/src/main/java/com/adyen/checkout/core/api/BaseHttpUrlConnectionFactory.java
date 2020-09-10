/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 24/04/2018.
 */

package com.adyen.checkout.core.api;

import static com.adyen.checkout.core.api.SSLSocketUtil.TLS_SOCKET_FACTORY;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

abstract class BaseHttpUrlConnectionFactory {

    static {
        HttpsURLConnection.setDefaultSSLSocketFactory(TLS_SOCKET_FACTORY);
    }

    @NonNull
    HttpURLConnection createHttpUrlConnection(@NonNull String url) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        if (urlConnection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(TLS_SOCKET_FACTORY);
            return urlConnection;
        } else {
            return handleInsecureConnection(urlConnection);
        }
    }

    @NonNull
    abstract HttpURLConnection handleInsecureConnection(@NonNull HttpURLConnection httpUrlConnection);
}
