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
import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import java.io.IOException
import org.json.JSONException
import org.json.JSONObject

private val TAG = LogUtil.getTag()
private const val ENDPOINT = "v1/order/status?clientKey="

class OrderStatusConnection(
    private val request: OrderStatusRequest,
    environment: Environment,
    clientKey: String
) : Connection<OrderStatusResponse>(
    "${environment.baseUrl}$ENDPOINT$clientKey"
) {
    @Throws(IOException::class, JSONException::class)
    override fun call(): OrderStatusResponse {
        Logger.v(TAG, "call - $url")
        val requestJson = OrderStatusRequest.SERIALIZER.serialize(request)
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")
        val result = post(CONTENT_TYPE_JSON_HEADER, requestJson.toString().toByteArray(Charsets.UTF_8))
        val resultJson = JSONObject(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return OrderStatusResponse.SERIALIZER.deserialize(resultJson)
    }
}
