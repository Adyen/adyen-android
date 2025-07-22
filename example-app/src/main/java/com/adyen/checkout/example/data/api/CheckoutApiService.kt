/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.api

import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.sessions.core.SessionModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.adyen.checkout.components.core.PaymentMethodsApiResponse as OldPaymentMethodsApiResponse

// TODO - Remove annotation after removing old functions
@Suppress("TooManyFunctions")
internal interface CheckoutApiService {

    @POST("sessions")
    suspend fun sessionsAsync(@Body sessionRequest: SessionRequest): SessionModel

    @POST("paymentMethods")
    suspend fun paymentMethodsAsyncOld(@Body paymentMethodsRequest: PaymentMethodsRequest): OldPaymentMethodsApiResponse

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

    @DELETE("storedPaymentMethods/{recurringId}")
    suspend fun removeStoredPaymentMethodAsync(
        @Path("recurringId") recurringId: String,
        @Query("merchantAccount") merchantAccount: String,
        @Query("shopperReference") shopperReference: String,
    ): Response<Unit>
}
