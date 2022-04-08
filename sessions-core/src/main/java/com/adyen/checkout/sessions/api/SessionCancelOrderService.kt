/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */

package com.adyen.checkout.sessions.api

import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val TAG = LogUtil.getTag()

internal class SessionCancelOrderService(
    private val environment: Environment,
) {

    suspend fun cancelOrder(
        request: SessionCancelOrderRequest,
        sessionId: String,
        clientKey: String,
    ): SessionCancelOrderResponse = withContext(Dispatchers.IO) {
        val path = "v1/sessions/$sessionId/orders/cancel?clientKey=$clientKey"

        Logger.v(TAG, "call - $path")

        val requestJson = SessionCancelOrderRequest.SERIALIZER.serialize(request)

        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")

        val httpClient = HttpClientFactory.getHttpClient(environment.baseUrl)
        val result = httpClient.post(path, requestJson.toString())
        val resultJson = JSONObject(String(result, Charsets.UTF_8))

        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")

        SessionCancelOrderResponse.SERIALIZER.deserialize(resultJson)
    }
}
