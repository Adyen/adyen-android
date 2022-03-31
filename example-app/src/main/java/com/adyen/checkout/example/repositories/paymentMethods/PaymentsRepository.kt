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
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.example.repositories.safeApiCall
import com.adyen.checkout.sessions.model.Session
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

interface PaymentsRepository {
    suspend fun getSessionAsync(sessionRequest: SessionRequest): Session?
    suspend fun getPaymentMethods(paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse?
    fun paymentsRequest(paymentsRequest: RequestBody): Call<ResponseBody>
    suspend fun paymentsRequestAsync(paymentsRequest: RequestBody): ResponseBody?
    fun detailsRequest(paymentsRequest: RequestBody): Call<ResponseBody>
    suspend fun detailsRequestAsync(paymentsRequest: RequestBody): ResponseBody?
    suspend fun balanceRequestAsync(balanceRequest: RequestBody): ResponseBody?
    suspend fun createOrderAsync(orderRequest: RequestBody): ResponseBody?
    suspend fun cancelOrderAsync(orderRequest: RequestBody): ResponseBody?
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

    override fun paymentsRequest(paymentsRequest: RequestBody): Call<ResponseBody> {
        return checkoutApiService.payments(paymentsRequest)
    }

    override suspend fun paymentsRequestAsync(paymentsRequest: RequestBody): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.paymentsAsync(paymentsRequest) }
        )
    }

    override fun detailsRequest(paymentsRequest: RequestBody): Call<ResponseBody> {
        return checkoutApiService.details(paymentsRequest)
    }

    override suspend fun detailsRequestAsync(paymentsRequest: RequestBody): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.detailsAsync(paymentsRequest) }
        )
    }

    override suspend fun balanceRequestAsync(balanceRequest: RequestBody): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.checkBalanceAsync(balanceRequest) }
        )
    }

    override suspend fun createOrderAsync(orderRequest: RequestBody): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.createOrderAsync(orderRequest) }
        )
    }

    override suspend fun cancelOrderAsync(orderRequest: RequestBody): ResponseBody? {
        return safeApiCall(
            call = { checkoutApiService.cancelOrderAsync(orderRequest) }
        )
    }
}
