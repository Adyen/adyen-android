/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/4/2022.
 */

package com.adyen.checkout.core.api

interface HttpClient {

    suspend fun get(
        path: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ByteArray

    suspend fun post(
        path: String,
        jsonBody: String,
        queryParameters: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ByteArray
}
