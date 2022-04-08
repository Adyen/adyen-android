/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/4/2022.
 */

package com.adyen.checkout.sessions.api

import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.sessions.model.orders.SessionBalanceRequest
import com.adyen.checkout.sessions.model.orders.SessionBalanceResponse
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderResponse
import com.adyen.checkout.sessions.model.orders.SessionOrderRequest
import com.adyen.checkout.sessions.model.orders.SessionOrderResponse
import com.adyen.checkout.sessions.model.payments.SessionDetailsRequest
import com.adyen.checkout.sessions.model.payments.SessionDetailsResponse
import com.adyen.checkout.sessions.model.payments.SessionPaymentsRequest
import com.adyen.checkout.sessions.model.payments.SessionPaymentsResponse
import com.adyen.checkout.sessions.model.setup.SessionSetupRequest
import com.adyen.checkout.sessions.model.setup.SessionSetupResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class SessionService(
    private val baseUrl: String,
) {

    suspend fun setupSession(
        request: SessionSetupRequest,
        sessionId: String,
        clientKey: String,
    ): SessionSetupResponse = withContext(Dispatchers.IO) {
        val resultJson = makeRequest(
            path = "v1/sessions/$sessionId/setup?clientKey=$clientKey",
            requestJson = SessionSetupRequest.SERIALIZER.serialize(request),
        )

        SessionSetupResponse.SERIALIZER.deserialize(resultJson)
    }

    suspend fun submitPayment(
        request: SessionPaymentsRequest,
        sessionId: String,
        clientKey: String,
    ): SessionPaymentsResponse = withContext(Dispatchers.IO) {
        val resultJson = makeRequest(
            path = "v1/sessions/$sessionId/payments?clientKey=$clientKey",
            requestJson = SessionPaymentsRequest.SERIALIZER.serialize(request),
        )

        SessionPaymentsResponse.SERIALIZER.deserialize(resultJson)
    }

    suspend fun submitDetails(
        request: SessionDetailsRequest,
        sessionId: String,
        clientKey: String,
    ): SessionDetailsResponse = withContext(Dispatchers.IO) {
        val resultJson = makeRequest(
            path = "v1/sessions/$sessionId/paymentDetails?clientKey=$clientKey",
            requestJson = SessionDetailsRequest.SERIALIZER.serialize(request),
        )

        SessionDetailsResponse.SERIALIZER.deserialize(resultJson)
    }

    suspend fun checkBalance(
        request: SessionBalanceRequest,
        sessionId: String,
        clientKey: String,
    ): SessionBalanceResponse = withContext(Dispatchers.IO) {
        val resultJson = makeRequest(
            path = "v1/sessions/$sessionId/paymentMethodBalance?clientKey=$clientKey",
            requestJson = SessionBalanceRequest.SERIALIZER.serialize(request),
        )

        SessionBalanceResponse.SERIALIZER.deserialize(resultJson)
    }

    suspend fun createOrder(
        request: SessionOrderRequest,
        sessionId: String,
        clientKey: String,
    ): SessionOrderResponse = withContext(Dispatchers.IO) {
        val resultJson = makeRequest(
            path = "v1/sessions/$sessionId/orders?clientKey=$clientKey",
            requestJson = SessionOrderRequest.SERIALIZER.serialize(request),
        )

        SessionOrderResponse.SERIALIZER.deserialize(resultJson)
    }

    suspend fun cancelOrder(
        request: SessionCancelOrderRequest,
        sessionId: String,
        clientKey: String,
    ): SessionCancelOrderResponse = withContext(Dispatchers.IO) {
        val resultJson = makeRequest(
            path = "v1/sessions/$sessionId/orders/cancel?clientKey=$clientKey",
            requestJson = SessionCancelOrderRequest.SERIALIZER.serialize(request),
        )

        SessionCancelOrderResponse.SERIALIZER.deserialize(resultJson)
    }

    private fun makeRequest(
        path: String,
        requestJson: JSONObject,
    ): JSONObject {
        Logger.v(TAG, "call - $path")
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")

        val httpClient = HttpClientFactory.getHttpClient(baseUrl)
        val result = httpClient.post(path, requestJson.toString())
        val resultJson = JSONObject(String(result, Charsets.UTF_8))

        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")

        return resultJson
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
