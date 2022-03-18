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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface CheckoutApiService {

    companion object {
        private const val defaultGradleUrl = "<YOUR_SERVER_URL>"

        fun isRealUrlAvailable(): Boolean {
            return BuildConfig.MERCHANT_SERVER_URL != defaultGradleUrl
        }
    }

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("sessions")
    suspend fun sessionsAsync(@Body sessionRequest: SessionRequest): Response<Session>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("paymentMethods")
    suspend fun paymentMethodsAsync(@Body paymentMethodsRequest: PaymentMethodsRequest): Response<PaymentMethodsApiResponse>

    // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments")
    fun payments(@Body paymentsRequest: RequestBody): Call<ResponseBody>

    // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments")
    suspend fun paymentsAsync(@Body paymentsRequest: RequestBody): Response<ResponseBody>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments/details")
    fun details(@Body detailsRequest: RequestBody): Call<ResponseBody>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments/details")
    suspend fun detailsAsync(@Body detailsRequest: RequestBody): Response<ResponseBody>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("paymentMethods/balance")
    suspend fun checkBalanceAsync(@Body balanceRequest: RequestBody): Response<ResponseBody>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("orders")
    suspend fun createOrderAsync(@Body orderRequest: RequestBody): Response<ResponseBody>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("orders/cancel")
    suspend fun cancelOrderAsync(@Body orderRequest: RequestBody): Response<ResponseBody>
}
