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
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.sessions.model.Session
import okhttp3.ResponseBody
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

    // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
    @POST("payments")
    fun payments(@Body paymentsRequest: PaymentsRequest): Call<ResponseBody>

    // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
    @POST("payments")
    suspend fun paymentsAsync(@Body paymentsRequest: PaymentsRequest): ResponseBody

    @POST("payments/details")
    fun details(@Body detailsRequest: JSONObject): Call<ResponseBody>

    @POST("payments/details")
    suspend fun detailsAsync(@Body detailsRequest: JSONObject): ResponseBody

    @POST("paymentMethods/balance")
    suspend fun checkBalanceAsync(@Body request: BalanceRequest): ResponseBody

    @POST("orders")
    suspend fun createOrderAsync(@Body orderRequest: CreateOrderRequest): ResponseBody

    @POST("orders/cancel")
    suspend fun cancelOrderAsync(@Body request: CancelOrderRequest): ResponseBody
}
