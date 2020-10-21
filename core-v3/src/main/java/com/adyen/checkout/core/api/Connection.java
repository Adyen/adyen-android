/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 08/09/2017.
 */

package com.adyen.checkout.core.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A wrapper for a callable network connection.
 *
 * @param <T> The type of the connection return.
 */
// TODO change to try-with-resources after updating min API lvl
@SuppressWarnings("PMD.CloseResource")
public abstract class Connection<T> implements Callable<T> {

    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APP_JSON_CONTENT_TYPE = "application/json";

    // Charset class only available from API 19
    @SuppressWarnings("CharsetObjectCanBeUsed")
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final int BUFFER_SIZE = 1024;

    private HttpURLConnection mURLConnection;
    private final String mUrl;

    protected Connection(@NonNull String url) {
        mUrl = url;
    }

    /**
     * @return the URl used to make this Connection.
     */
    @NonNull
    protected String getUrl() {
        return mUrl;
    }

    /**
     * Performs an URL connection using HTTP GET.
     *
     * @return The byte array of the response
     * @throws IOException In case an IO error happens.
     */
    @NonNull
    protected byte[] get() throws IOException {
        return get(Collections.<String, String>emptyMap());
    }

    /**
     * Performs an URL connection using HTTP GET.
     *
     * @param headers The headers of the connection.
     * @return The byte array of the response
     * @throws IOException n case an IO error happens.
     */
    @NonNull
    protected byte[] get(@NonNull Map<String, String> headers) throws IOException {
        if (mURLConnection != null) {
            throw new RuntimeException("Connection already initiated");
        }

        try {
            mURLConnection = getUrlConnection(mUrl, headers, HttpMethod.GET);
            mURLConnection.connect();

            return handleResponse(mURLConnection);
        } finally {
            if (mURLConnection != null) {
                mURLConnection.disconnect();
            }
        }
    }

    /**
     * Performs an URL connection using HTTP POST.
     *
     * @return The byte array of the response
     * @throws IOException In case an IO error happens.
     */
    @NonNull
    protected byte[] post(@NonNull Map<String, String> headers, @NonNull byte[] data) throws IOException {
        if (mURLConnection != null) {
            throw new RuntimeException("Connection already initiated");
        }

        try {
            mURLConnection = getUrlConnection(mUrl, headers, HttpMethod.POST);
            mURLConnection.connect();

            final OutputStream outputStream = mURLConnection.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();

            return handleResponse(mURLConnection);
        } finally {
            if (mURLConnection != null) {
                mURLConnection.disconnect();
            }
        }
    }

    @NonNull
    private IOException parseException(@Nullable byte[] errorBytes) {
        String message = null;

        if (errorBytes != null) {
            message = new String(errorBytes, CHARSET);
        }

        return new IOException(message);
    }

    @NonNull
    private HttpURLConnection getUrlConnection(@NonNull String url, @NonNull Map<String, String> headers, @NonNull HttpMethod httpMethod)
            throws IOException {

        final HttpURLConnection urlConnection = HttpUrlConnectionFactory.getInstance().createHttpUrlConnection(url);
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
    private byte[] handleResponse(@NonNull HttpURLConnection urlConnection) throws IOException {
        final InputStream errorStream = urlConnection.getErrorStream();

        if (errorStream == null) {
            final byte[] responseBytes = getBytes(urlConnection.getInputStream());
            if (responseBytes != null) {
                return responseBytes;
            }
        }

        final byte[] errorBytes = getBytes(errorStream);
        throw parseException(errorBytes);
    }

    @Nullable
    private byte[] getBytes(@Nullable InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buffer = new byte[BUFFER_SIZE];

        int length = inputStream.read(buffer);

        while (length > 0) {
            out.write(buffer, 0, length);
            length = inputStream.read(buffer);
        }

        inputStream.close();

        return out.toByteArray();
    }

    private enum HttpMethod {
        GET("GET", false),
        POST("POST", true);

        private final String mValue;
        private final boolean mDoOutput;

        HttpMethod(@NonNull String value, boolean doOutput) {
            mValue = value;
            mDoOutput = doOutput;
        }

        @NonNull
        String getValue() {
            return mValue;
        }

        boolean isDoOutput() {
            return mDoOutput;
        }
    }
}
