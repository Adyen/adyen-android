/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.repositories.paymentMethods

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.example.repositories.safeApiCall
import com.adyen.checkout.sessions.model.Session
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

interface PaymentsRepository {
    suspend fun getSessionAsync(sessionRequest: SessionRequest): Session?
    suspend fun getPaymentMethods(paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse?
    fun paymentsRequest(paymentsRequest: PaymentsRequest): Call<ResponseBody>
    suspend fun paymentsRequestAsync(paymentsRequest: PaymentsRequest): ResponseBody?
    fun detailsRequest(detailsRequest: JSONObject): Call<ResponseBody>
    suspend fun detailsRequestAsync(detailsRequest: JSONObject): ResponseBody?
    suspend fun balanceRequestAsync(request: BalanceRequest): ResponseBody?
    suspend fun createOrderAsync(orderRequest: CreateOrderRequest): ResponseBody?
    suspend fun cancelOrderAsync(request: CancelOrderRequest): ResponseBody?
}

internal class PaymentsRepositoryImpl(private val checkoutApiService: CheckoutApiService) : PaymentsRepository {

    override suspend fun getSessionAsync(sessionRequest: SessionRequest): Session? {
        return safeApiCall { checkoutApiService.sessionsAsync(sessionRequest) }
    }

    override suspend fun getPaymentMethods(paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse? {
        return safeApiCall(
            call = { checkoutApiService.paymentMethodsAsync(paymentMethodsRequest) }
        )
    }

    override fun paymentsRequest(paymentsRequest: PaymentsRequest): Call<ResponseBody> {
        return checkoutApiService.payments(paymentsRequest)
    }

    override suspend fun paymentsRequestAsync(paymentsRequest: PaymentsRequest): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.paymentsAsync(paymentsRequest) }
        )
    }

    override fun detailsRequest(detailsRequest: JSONObject): Call<ResponseBody> {
        return checkoutApiService.details(detailsRequest)
    }

    override suspend fun detailsRequestAsync(detailsRequest: JSONObject): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.detailsAsync(detailsRequest) }
        )
    }

    override suspend fun balanceRequestAsync(request: BalanceRequest): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.checkBalanceAsync(request) }
        )
    }

    override suspend fun createOrderAsync(orderRequest: CreateOrderRequest): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.createOrderAsync(orderRequest) }
        )
    }

    override suspend fun cancelOrderAsync(request: CancelOrderRequest): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.cancelOrderAsync(request) }
        )
    }
}
