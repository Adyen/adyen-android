/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2021.
 */

package com.adyen.checkout.components.api

import com.adyen.checkout.components.model.connection.OrderStatusRequest
import com.adyen.checkout.components.model.connection.OrderStatusResponse
import com.adyen.checkout.core.api.Connection.Companion.CONTENT_TYPE_JSON_HEADER
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val TAG = LogUtil.getTag()

internal class OrderStatusService(
    private val request: OrderStatusRequest,
    private val environment: Environment,
    clientKey: String
) {

    private val path = "v1/order/status?clientKey=$clientKey"

    suspend fun getOrderStatus(): OrderStatusResponse = withContext(Dispatchers.IO) {
        Logger.v(TAG, "call - $path")
        val requestJson = OrderStatusRequest.SERIALIZER.serialize(request)
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")

        val httpClient = HttpClientFactory.getHttpClient(environment.baseUrl)
        val result = httpClient.post(path, requestJson.toString(), CONTENT_TYPE_JSON_HEADER)
        val resultJson = JSONObject(String(result, Charsets.UTF_8))

        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")

        OrderStatusResponse.SERIALIZER.deserialize(resultJson)
    }
}
