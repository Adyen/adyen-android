/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/4/2019.
 */

package com.adyen.checkout.core.api;

import static org.junit.Assert.*;

import androidx.annotation.NonNull;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

public class BaseHttpUrlConnectionFactoryTest {

    @Test
    public void initBaseHttpUrl_Pass_HTTPS_ExpectSecureConnection() throws IOException {
        String url = "https://test.com";

        HttpURLConnection urlConnection = new BaseHttpUrlConnectionFactory() {
            @NonNull
            @Override
            HttpURLConnection handleInsecureConnection(@NonNull HttpURLConnection httpUrlConnection) {
                // this should get call
                assertEquals(1, 2);
                return httpUrlConnection;
            }
        }.createHttpUrlConnection(url);

        assertEquals(urlConnection.getURL().toString(), url);
    }

    @Test
    public void initBaseHttpUrl_Pass_HTTPS_ExpectInsecureConnection() throws IOException {
        final String url = "http://test.com";

        HttpURLConnection urlConnection = new BaseHttpUrlConnectionFactory() {
            @NonNull
            @Override
            HttpURLConnection handleInsecureConnection(@NonNull HttpURLConnection httpUrlConnection) {
                // this should get call
                assertEquals(httpUrlConnection.getURL().toString(), url);
                return httpUrlConnection;
            }
        }.createHttpUrlConnection(url);
    }
}