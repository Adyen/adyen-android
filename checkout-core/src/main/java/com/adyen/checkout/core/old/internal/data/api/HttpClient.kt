/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/4/2022.
 */

package com.adyen.checkout.core.old.internal.data.api

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface HttpClient {

    suspend fun get(
        path: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): AdyenApiResponse

    suspend fun post(
        path: String,
        jsonBody: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): AdyenApiResponse
}
