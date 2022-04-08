/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/4/2022.
 */

package com.adyen.checkout.core.api

import okhttp3.OkHttpClient
import com.adyen.checkout.core.api.OkHttpClient as InternalOkHttpClient

object HttpClientFactory {

    private val defaultHeaders = mapOf(
        "Content-Type" to "application/json"
    )

    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }

    fun getHttpClient(baseUrl: String): HttpClient {
        return InternalOkHttpClient(okHttpClient, baseUrl, defaultHeaders)
    }
}
