/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/4/2022.
 */

package com.adyen.checkout.core.api

interface HttpClient {

    fun get(path: String, headers: Map<String, String> = emptyMap()): ByteArray

    fun post(path: String, jsonBody: String, headers: Map<String, String> = emptyMap()): ByteArray
}
