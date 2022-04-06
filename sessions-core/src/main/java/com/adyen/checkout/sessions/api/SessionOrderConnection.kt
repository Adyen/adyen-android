/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.api

import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.sessions.model.orders.SessionOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionOrderResponse
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

private val TAG = LogUtil.getTag()

class SessionCreateOrderConnection(
    private val request: SessionOrderRequest,
    environment: Environment,
    sessionId: String,
    clientKey: String
) : Connection<SessionOrderResponse>(environment.baseUrl) {

    private val path = "v1/sessions/$sessionId/orders?clientKey=$clientKey"

    @Throws(IOException::class, JSONException::class)
    override fun call(): SessionOrderResponse {
        Logger.v(TAG, "call - $path")
        val requestJson = SessionOrderRequest.SERIALIZER.serialize(request)
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")
        val result = post(path, requestJson.toString(), CONTENT_TYPE_JSON_HEADER)
        val resultJson = JSONObject(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return SessionOrderResponse.SERIALIZER.deserialize(resultJson)
    }
}
