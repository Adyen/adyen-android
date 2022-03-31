/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/3/2022.
 */

package com.adyen.checkout.sessions.api

import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.sessions.model.request.SessionCancelOrderRequest
import com.adyen.checkout.sessions.model.response.SessionCancelOrderResponse
import java.io.IOException
import org.json.JSONException
import org.json.JSONObject

private val TAG = LogUtil.getTag()
private const val ENDPOINT = "v1/sessions/"

class SessionCancelOrderConnection(
    private val request: SessionCancelOrderRequest,
    environment: Environment,
    sessionId: String,
    clientKey: String
) : Connection<SessionCancelOrderResponse>(
    "${environment.baseUrl}$ENDPOINT$sessionId/orders/cancel?clientKey=$clientKey"
) {
    @Throws(IOException::class, JSONException::class)
    override fun call(): SessionCancelOrderResponse {
        Logger.v(TAG, "call - $url")
        val requestJson = SessionCancelOrderRequest.SERIALIZER.serialize(request)
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")
        val result = post(CONTENT_TYPE_JSON_HEADER, requestJson.toString().toByteArray(Charsets.UTF_8))
        val resultJson = JSONObject(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return SessionCancelOrderResponse.SERIALIZER.deserialize(resultJson)
    }
}
