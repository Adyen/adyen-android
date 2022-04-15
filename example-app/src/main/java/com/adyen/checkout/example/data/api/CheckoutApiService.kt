/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.api

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.sessions.model.Session
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface CheckoutApiService {

    companion object {
        private const val defaultGradleUrl = "<YOUR_SERVER_URL>"

        fun isRealUrlAvailable(): Boolean {
            return BuildConfig.MERCHANT_SERVER_URL != defaultGradleUrl
        }
    }

    @POST("sessions")
    suspend fun sessionsAsync(@Body sessionRequest: SessionRequest): Session

    @POST("paymentMethods")
    suspend fun paymentMethodsAsync(@Body paymentMethodsRequest: PaymentMethodsRequest): PaymentMethodsApiResponse

    @POST("payments")
    fun payments(@Body paymentsRequest: JSONObject): Call<JSONObject>

    @POST("payments")
    suspend fun paymentsAsync(@Body paymentsRequest: JSONObject): JSONObject

    @POST("payments/details")
    fun details(@Body detailsRequest: JSONObject): Call<JSONObject>

    @POST("payments/details")
    suspend fun detailsAsync(@Body detailsRequest: JSONObject): JSONObject

    @POST("paymentMethods/balance")
    suspend fun checkBalanceAsync(@Body request: BalanceRequest): JSONObject

    @POST("orders")
    suspend fun createOrderAsync(@Body orderRequest: CreateOrderRequest): JSONObject

    @POST("orders/cancel")
    suspend fun cancelOrderAsync(@Body request: CancelOrderRequest): JSONObject
}
