/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.api

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal abstract class BaseHttpUrlConnectionFactory {

    @Throws(IOException::class)
    fun createHttpUrlConnection(url: String): HttpURLConnection {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        return if (urlConnection is HttpsURLConnection) {
            urlConnection.sslSocketFactory = SSLSocketUtil.TLS_SOCKET_FACTORY
            urlConnection
        } else {
            handleInsecureConnection(urlConnection)
        }
    }

    abstract fun handleInsecureConnection(httpUrlConnection: HttpURLConnection): HttpURLConnection

    companion object {
        init {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketUtil.TLS_SOCKET_FACTORY)
        }
    }
}
