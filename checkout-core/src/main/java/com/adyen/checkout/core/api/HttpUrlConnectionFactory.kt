/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.api

import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * A factory that creates a URL connections using a secure socket encryption.
 */
internal object HttpUrlConnectionFactory {

    private val TAG = LogUtil.getTag()
    private const val ERROR_MESSAGE_INSECURE_CONNECTION = "Trying to connect to a URL that is not HTTPS."

    init {
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketUtil.TLS_SOCKET_FACTORY)
    }

    @Throws(IOException::class)
    fun createHttpUrlConnection(
        url: String,
        onInsecureConnection: (httpUrlConnection: HttpURLConnection) -> HttpURLConnection = ::handleInsecureConnection,
    ): HttpURLConnection {
        val urlConnection = URL(url).openConnection()
        return if (urlConnection is HttpsURLConnection) {
            urlConnection.sslSocketFactory = SSLSocketUtil.TLS_SOCKET_FACTORY
            urlConnection
        } else {
            onInsecureConnection(urlConnection as HttpURLConnection)
        }
    }

    private fun handleInsecureConnection(httpUrlConnection: HttpURLConnection): HttpURLConnection {
        Logger.w(TAG, ERROR_MESSAGE_INSECURE_CONNECTION)
        return httpUrlConnection
    }
}
