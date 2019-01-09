/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 08/09/2017.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class Api {
    @NonNull
    public static final Charset CHARSET = Charset.forName("UTF-8");

    private static final int BUFFER_SIZE = 1024;

    private BaseHttpUrlConnectionFactory mHttpUrlConnectionFactory = new HttpUrlConnectionFactory();

    @NonNull
    protected byte[] get(@NonNull String url, @NonNull Map<String, String> headers) throws IOException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = getUrlConnection(url, headers, HttpMethod.GET);
            urlConnection.connect();

            InputStream errorStream = urlConnection.getErrorStream();

            return handleResponse(errorStream == null ? urlConnection.getInputStream() : null, errorStream);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @NonNull
    protected byte[] post(@NonNull String url, @NonNull Map<String, String> headers, @NonNull byte[] data) throws IOException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = getUrlConnection(url, headers, HttpMethod.POST);
            urlConnection.connect();

            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();

            InputStream errorStream = urlConnection.getErrorStream();

            return handleResponse(errorStream == null ? urlConnection.getInputStream() : null, errorStream);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @NonNull
    protected IOException parseException(@Nullable byte[] errorBytes) {
        String message = null;

        if (errorBytes != null) {
            message = new String(errorBytes, CHARSET);
        }

        return new IOException(message);
    }

    @NonNull
    private HttpURLConnection getUrlConnection(@NonNull String url, @NonNull Map<String, String> headers, @NonNull HttpMethod httpMethod)
            throws IOException {
        HttpURLConnection urlConnection = mHttpUrlConnectionFactory.createHttpUrlConnection(url);
        urlConnection.setRequestMethod(httpMethod.getValue());
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(httpMethod.isDoOutput());

        for (Map.Entry<String, String> header : headers.entrySet()) {
            urlConnection.addRequestProperty(header.getKey(), header.getValue());
        }

        return urlConnection;
    }

    @NonNull
    private byte[] handleResponse(@Nullable InputStream inputStream, @Nullable InputStream errorStream) throws IOException {
        byte[] responseBytes = getBytes(inputStream);

        if (responseBytes != null) {
            return responseBytes;
        }

        byte[] errorBytes = getBytes(errorStream);

        throw parseException(errorBytes);
    }

    @Nullable
    private byte[] getBytes(@Nullable InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        inputStream.close();

        return out.toByteArray();
    }

    private enum HttpMethod {
        GET("GET", false),
        POST("POST", true);

        private String mValue;

        private boolean mDoOutput;

        HttpMethod(@NonNull String value, boolean doOutput) {
            mValue = value;
            mDoOutput = doOutput;
        }

        @NonNull
        private String getValue() {
            return mValue;
        }

        private boolean isDoOutput() {
            return mDoOutput;
        }
    }
}
