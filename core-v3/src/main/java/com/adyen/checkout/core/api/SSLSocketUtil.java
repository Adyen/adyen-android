/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/7/2019.
 */

package com.adyen.checkout.core.api;

import androidx.annotation.NonNull;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Util Class for SSL Socket.
 */
@SuppressWarnings("AbbreviationAsWordInName")
public final class SSLSocketUtil {

    public static final SSLSocketFactory TLS_SOCKET_FACTORY;

    static {
        try {
            TLS_SOCKET_FACTORY = getTLSSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Could not initialize SSLSocketFactory.", e);
        }
    }

    /**
     * Get an SSL factory from SSLContext with TLS enabled.
     *
     * @return SSLSocketFactory depends on version code.
     */
    @NonNull
    private static SSLSocketFactory getTLSSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        return context.getSocketFactory();
    }

    private SSLSocketUtil() {
        throw new AssertionError("private constructor");
    }
}
