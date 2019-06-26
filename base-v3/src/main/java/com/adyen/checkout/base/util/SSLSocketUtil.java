/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/6/2019.
 */

package com.adyen.checkout.base.util;

import android.os.Build;
import android.support.annotation.NonNull;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Util Class for SSL Socket.
 */
@SuppressWarnings("AbbreviationAsWordInName")
public final class SSLSocketUtil {

    private SSLSocketUtil() {
        throw new AssertionError("private constructor");
    }

    /**
     * get SSL factory from SSLContext.
     *
     * @return SSLSocketFactory depends on version code.
     */
    @NonNull
    public static SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        final SSLSocketFactory v3Factory;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            v3Factory = new TLSSocketFactory(context.getSocketFactory());
        } else {
            v3Factory = context.getSocketFactory();
        }

        return v3Factory;
    }
}
