/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/1/2022.
 */

package com.adyen.checkout.example.data.api

import com.adyen.checkout.example.BuildConfig
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RecurringApiService {

    companion object {
        private const val defaultGradleUrl = "<YOUR_SERVER_URL>"

        fun isRealUrlAvailable(): Boolean {
            return BuildConfig.MERCHANT_RECURRING_SERVER_URL != defaultGradleUrl
        }
    }

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("disable")
    fun removeStoredPaymentMethodAsync(@Body request: RequestBody): Deferred<Response<ResponseBody>>
}
