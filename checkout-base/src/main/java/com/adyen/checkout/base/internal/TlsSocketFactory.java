/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 06/10/2017.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TlsSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory mInternalSslSocketFactory;

    private String[] mEnabledProtocols;

    TlsSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        mInternalSslSocketFactory = context.getSocketFactory();
        mEnabledProtocols = new String[]{"TLSv1.2"};
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return mInternalSslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mInternalSslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTlsOnSocket(mInternalSslSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTlsOnSocket(mInternalSslSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTlsOnSocket(mInternalSslSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTlsOnSocket(mInternalSslSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTlsOnSocket(mInternalSslSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTlsOnSocket(mInternalSslSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    @Nullable
    private Socket enableTlsOnSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(mEnabledProtocols);
        }

        return socket;
    }
}
