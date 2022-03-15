/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.api

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

/**
 * Util Class for SSL Socket.
 */

object SSLSocketUtil {

    /**
     * Get an SSL factory from SSLContext with TLS enabled.
     */
    @JvmStatic
    @get:Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    val TLS_SOCKET_FACTORY: SSLSocketFactory
        get() {
            val context = SSLContext.getInstance("TLS")
            context.init(null, null, null)
            return context.socketFactory
        }
}
