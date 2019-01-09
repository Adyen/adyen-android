/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 24/04/2018.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public abstract class BaseHttpUrlConnectionFactory {
    private static final SSLSocketFactory SSL_SOCKET_FACTORY;

    static final String ERROR_MESSAGE_INSECURE_CONNECTION = "Trying to connect to a URL that is not HTTPS.";

    static {
        try {
            SSL_SOCKET_FACTORY = new TlsSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Could not initialize SSLSocketFactory.");
        }
    }

    @NonNull
    HttpURLConnection createHttpUrlConnection(@NonNull String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        if (urlConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
            httpsURLConnection.setSSLSocketFactory(SSL_SOCKET_FACTORY);

            return httpsURLConnection;
        } else {
            return handleInsecureConnection(urlConnection);
        }
    }

    @NonNull
    abstract HttpURLConnection handleInsecureConnection(@NonNull HttpURLConnection httpUrlConnection);
}
