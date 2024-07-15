/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.test.server

import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException

object CheckoutMockWebServer {

    private const val DEFAULT_PORT = 8080

    const val baseUrl = "http://127.0.0.1:8080/"

    private var mockWebServer: MockWebServer? = null

    fun start(): MockWebServer {
        stop()

        val newServer = MockWebServer()
        mockWebServer = newServer

        newServer.dispatcher = DelegatingBackendDispatcher()

        try {
            newServer.start(DEFAULT_PORT)
        } catch (e: IOException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Failed to start mock web server." }
        }

        return newServer
    }

    fun stop() {
        try {
            mockWebServer?.let {
                it.shutdown()
                mockWebServer = null
            }
        } catch (e: IOException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Failed to stop mock web server." }
        }
    }
}
