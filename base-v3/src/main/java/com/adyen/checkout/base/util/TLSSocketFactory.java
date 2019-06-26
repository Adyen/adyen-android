/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/6/2019.
 */

package com.adyen.checkout.base.util;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

@SuppressWarnings("AbbreviationAsWordInName")
public class TLSSocketFactory extends SSLSocketFactory {
    public static final String TLS_V12 = "TLSv1.2";

    private final String[] mEnabledProtocols;

    private final SSLSocketFactory mDelegate;

    public TLSSocketFactory(@NonNull SSLSocketFactory base) {
        this.mDelegate = base;
        mEnabledProtocols = new String[]{TLS_V12};
    }

    @Override
    @NonNull
    public String[] getDefaultCipherSuites() {
        return mDelegate.getDefaultCipherSuites();
    }

    @Override
    @NonNull
    public String[] getSupportedCipherSuites() {
        return mDelegate.getSupportedCipherSuites();
    }

    @Override
    @NonNull
    public Socket createSocket(@NonNull Socket s, @NonNull String host, @NonNull int port, @NonNull boolean autoClose)
            throws IOException {
        return patch(mDelegate.createSocket(s, host, port, autoClose));
    }

    @Override
    @NonNull
    public Socket createSocket(@NonNull String host, @NonNull int port) throws IOException {
        return patch(mDelegate.createSocket(host, port));
    }

    @Override
    @NonNull
    public Socket createSocket(@NonNull String host, @NonNull int port, @NonNull InetAddress localHost, @NonNull int localPort)
            throws IOException {
        return patch(mDelegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    @NonNull
    public Socket createSocket(@NonNull InetAddress host, @NonNull int port) throws IOException {
        return patch(mDelegate.createSocket(host, port));
    }

    @Override
    @NonNull
    public Socket createSocket(@NonNull InetAddress address, @NonNull int port, @NonNull InetAddress localAddress, @NonNull int localPort)
            throws IOException {
        return patch(mDelegate.createSocket(address, port, localAddress, localPort));
    }

    @NonNull
    private Socket patch(Socket s) {
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).setEnabledProtocols(mEnabledProtocols);
        }
        return s;
    }
}
