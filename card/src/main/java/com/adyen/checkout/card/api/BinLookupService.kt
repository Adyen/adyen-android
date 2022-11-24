/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/1/2021.
 */

package com.adyen.checkout.card.api

import com.adyen.checkout.card.api.model.BinLookupRequest
import com.adyen.checkout.card.api.model.BinLookupResponse
import com.adyen.checkout.core.api.HttpClient
import com.adyen.checkout.core.api.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class BinLookupService(
    private val httpClient: HttpClient,
) {

    suspend fun makeBinLookup(
        request: BinLookupRequest,
        clientKey: String,
    ): BinLookupResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v2/bin/binLookup",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = BinLookupRequest.SERIALIZER,
            responseSerializer = BinLookupResponse.SERIALIZER
        )
    }
}
