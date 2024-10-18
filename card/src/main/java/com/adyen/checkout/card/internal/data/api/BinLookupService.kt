/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.card.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.internal.data.model.BinLookupRequest
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BinLookupService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    suspend fun makeBinLookup(
        request: BinLookupRequest,
        clientKey: String,
    ): BinLookupResponse = withContext(coroutineDispatcher) {
        httpClient.post(
            path = "v2/bin/binLookup",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = BinLookupRequest.SERIALIZER,
            responseSerializer = BinLookupResponse.SERIALIZER,
        )
    }
}
