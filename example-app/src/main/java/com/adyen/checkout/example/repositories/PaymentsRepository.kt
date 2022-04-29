/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.repositories

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequestData
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.sessions.model.Session
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject

interface PaymentsRepository {
    suspend fun getSessionAsync(sessionRequest: SessionRequest): Session?
    suspend fun getPaymentMethods(paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse?
    fun paymentsRequest(paymentsRequest: PaymentsRequest): JSONObject?
    suspend fun paymentsRequestAsync(paymentsRequest: PaymentsRequest): JSONObject?
    fun detailsRequest(detailsRequest: JSONObject): JSONObject?
    suspend fun detailsRequestAsync(detailsRequest: JSONObject): JSONObject?
    suspend fun balanceRequestAsync(request: BalanceRequest): JSONObject?
    suspend fun createOrderAsync(orderRequest: CreateOrderRequest): JSONObject?
    suspend fun cancelOrderAsync(request: CancelOrderRequest): JSONObject?
}

@Suppress("TooManyFunctions")
internal class PaymentsRepositoryImpl(private val checkoutApiService: CheckoutApiService) : PaymentsRepository {

    override suspend fun getSessionAsync(sessionRequest: SessionRequest): Session? {
        return safeApiCall { checkoutApiService.sessionsAsync(sessionRequest) }
    }

    override suspend fun getPaymentMethods(paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse? {
        return safeApiCall(
            call = { checkoutApiService.paymentMethodsAsync(paymentMethodsRequest) }
        )
    }

    override fun paymentsRequest(paymentsRequest: PaymentsRequest): JSONObject? {
        return checkoutApiService.payments(paymentsRequest.combineToJSONObject()).execute().body()
    }

    override suspend fun paymentsRequestAsync(paymentsRequest: PaymentsRequest): JSONObject? {
        return safeApiCall(
            call = { checkoutApiService.paymentsAsync(paymentsRequest.combineToJSONObject()) }
        )
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

    override fun detailsRequest(detailsRequest: JSONObject): JSONObject? {
        return checkoutApiService.details(detailsRequest).execute().body()
    }

    override suspend fun detailsRequestAsync(detailsRequest: JSONObject): JSONObject? {
        return safeApiCall(
            call = { checkoutApiService.detailsAsync(detailsRequest) }
        )
    }

    override suspend fun balanceRequestAsync(request: BalanceRequest): JSONObject? {
        return safeApiCall(
            call = { checkoutApiService.checkBalanceAsync(request) }
        )
    }

    override suspend fun createOrderAsync(orderRequest: CreateOrderRequest): JSONObject? {
        return safeApiCall(
            call = { checkoutApiService.createOrderAsync(orderRequest) }
        )
    }

    override suspend fun cancelOrderAsync(request: CancelOrderRequest): JSONObject? {
        return safeApiCall(
            call = { checkoutApiService.cancelOrderAsync(request) }
        )
    }
}
