/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Environment
import okhttp3.OkHttpClient
import com.adyen.checkout.core.internal.data.api.OkHttpClient as InternalOkHttpClient

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object HttpClientFactory {

    private val defaultHeaders = mapOf(
        "Content-Type" to "application/json"
    )

    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }

    fun getHttpClient(environment: Environment): HttpClient {
        return InternalOkHttpClient(
            okHttpClient,
            environment.checkoutShopperBaseUrl.toString(),
            defaultHeaders
        )
    }

    fun getAnalyticsHttpClient(environment: Environment): HttpClient {
        return InternalOkHttpClient(
            okHttpClient,
            environment.checkoutAnalyticsBaseUrl.toString(),
            defaultHeaders
        )
    }
}
