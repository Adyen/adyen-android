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
import java.net.HttpURLConnection

/**
 * A factory that creates a URL connections using a secure socket encryption.
 */
internal class HttpUrlConnectionFactory private constructor() : BaseHttpUrlConnectionFactory() {

    override fun handleInsecureConnection(httpUrlConnection: HttpURLConnection): HttpURLConnection {
        Logger.w(TAG, ERROR_MESSAGE_INSECURE_CONNECTION)
        return httpUrlConnection
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val ERROR_MESSAGE_INSECURE_CONNECTION = "Trying to connect to a URL that is not HTTPS."

        /**
         * Get the instance of the [HttpUrlConnectionFactory].
         *
         * @return The instance of the [HttpUrlConnectionFactory].
         */
        @JvmStatic
        val INSTANCE: HttpUrlConnectionFactory by lazy { HttpUrlConnectionFactory() }
    }
}
