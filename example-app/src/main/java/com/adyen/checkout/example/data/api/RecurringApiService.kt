/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/1/2022.
 */

package com.adyen.checkout.example.data.api

import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.api.model.RemoveStoredPaymentMethodRequest
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST

internal interface RecurringApiService {

    companion object {
        private const val defaultGradleUrl = "<YOUR_SERVER_URL>"

        fun isRealUrlAvailable(): Boolean {
            return BuildConfig.MERCHANT_RECURRING_SERVER_URL != defaultGradleUrl
        }
    }

    @POST("disable")
    suspend fun removeStoredPaymentMethodAsync(@Body request: RemoveStoredPaymentMethodRequest): JSONObject
}
