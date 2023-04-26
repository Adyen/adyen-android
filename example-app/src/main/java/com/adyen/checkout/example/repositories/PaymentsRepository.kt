/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.repositories

import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequestData
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.sessions.core.SessionModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject

interface PaymentsRepository {
    suspend fun createSession(sessionRequest: SessionRequest): SessionModel?
    suspend fun getPaymentMethods(paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse?
    suspend fun makePaymentsRequest(paymentsRequest: PaymentsRequest): JSONObject?
    suspend fun makeDetailsRequest(detailsRequest: JSONObject): JSONObject?
    suspend fun getBalance(request: BalanceRequest): JSONObject?
    suspend fun createOrder(orderRequest: CreateOrderRequest): JSONObject?
    suspend fun cancelOrder(request: CancelOrderRequest): JSONObject?
}

@Suppress("TooManyFunctions")
internal class PaymentsRepositoryImpl(private val checkoutApiService: CheckoutApiService) : PaymentsRepository {

    override suspend fun createSession(sessionRequest: SessionRequest): SessionModel? = safeApiCall {
        checkoutApiService.sessionsAsync(sessionRequest)
    }

    override suspend fun getPaymentMethods(
        paymentMethodsRequest: PaymentMethodsRequest
    ): PaymentMethodsApiResponse? = safeApiCall {
        checkoutApiService.paymentMethodsAsync(paymentMethodsRequest)
    }

    override suspend fun makePaymentsRequest(paymentsRequest: PaymentsRequest): JSONObject? = safeApiCall {
        checkoutApiService.paymentsAsync(paymentsRequest.combineToJSONObject())
    }

    private fun PaymentsRequest.combineToJSONObject(): JSONObject {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(PaymentsRequestData::class.java)
        val requestDataJson = JSONObject(adapter.toJson(this.requestData))

        return requestDataJson
            // This will override any already existing fields in requestDataJson
            .putAll(this.paymentComponentData)
    }

    private fun JSONObject.putAll(other: JSONObject): JSONObject {
        val keys = other.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = other.get(key)
            put(key, value)
        }
        return this
    }

    override suspend fun makeDetailsRequest(detailsRequest: JSONObject): JSONObject? = safeApiCall {
        checkoutApiService.detailsAsync(detailsRequest)
    }

    override suspend fun getBalance(request: BalanceRequest): JSONObject? = safeApiCall {
        checkoutApiService.checkBalanceAsync(request)
    }

    override suspend fun createOrder(orderRequest: CreateOrderRequest): JSONObject? = safeApiCall {
        checkoutApiService.createOrderAsync(orderRequest)
    }

    override suspend fun cancelOrder(request: CancelOrderRequest): JSONObject? = safeApiCall {
        checkoutApiService.cancelOrderAsync(request)
    }
}
