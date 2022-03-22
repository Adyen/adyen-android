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
import com.adyen.checkout.example.data.api.model.paymentsRequest.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.paymentsRequest.SessionRequest
import com.adyen.checkout.sessions.model.Session
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    fun payments(@Body paymentsRequest: RequestBody): Call<ResponseBody>

    // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
    @POST("payments")
    suspend fun paymentsAsync(@Body paymentsRequest: RequestBody): ResponseBody

    @POST("payments/details")
    fun details(@Body detailsRequest: RequestBody): Call<ResponseBody>

    @POST("payments/details")
    suspend fun detailsAsync(@Body detailsRequest: RequestBody): ResponseBody

    @POST("paymentMethods/balance")
    suspend fun checkBalanceAsync(@Body balanceRequest: RequestBody): ResponseBody

    @POST("orders")
    suspend fun createOrderAsync(@Body orderRequest: RequestBody): ResponseBody

    @POST("orders/cancel")
    suspend fun cancelOrderAsync(@Body orderRequest: RequestBody): ResponseBody
}
