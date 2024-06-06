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
import com.adyen.checkout.test.server.service.MockCheckoutService
import com.adyen.checkout.test.server.service.MockPublicKeyService
import com.adyen.checkout.test.server.service.MockSessionService
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND

internal class DelegatingBackendDispatcher : Dispatcher() {

    private val delegates = listOf(
        MockCheckoutService(),
        MockPublicKeyService(),
        MockSessionService(),
    )

    override fun dispatch(request: RecordedRequest): MockResponse = try {
        val service = delegates.firstOrNull { it.canHandleRequest(request) }

        service?.handleRequest(request)
            ?: MockResponse().setResponseCode(HTTP_NOT_FOUND).setBody("Resource not found")
    } catch (e: Exception) {
        adyenLog(AdyenLogLevel.ERROR, e) { "Could not handle request: $request" }
        MockResponse().setResponseCode(HTTP_INTERNAL_ERROR).setBody("Unexpected error")
    }
}
