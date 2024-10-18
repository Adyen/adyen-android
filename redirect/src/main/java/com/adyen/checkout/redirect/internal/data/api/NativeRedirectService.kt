/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/10/2023.
 */

package com.adyen.checkout.redirect.internal.data.api

import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class NativeRedirectService(
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher = DispatcherProvider.IO
) {

    suspend fun makeNativeRedirect(
        request: NativeRedirectRequest,
        clientKey: String,
    ): NativeRedirectResponse = withContext(dispatcher) {
        httpClient.post(
            path = "v1/nativeRedirect/redirectResult",
            queryParameters = mapOf("clientKey" to clientKey),
            body = request,
            requestSerializer = NativeRedirectRequest.SERIALIZER,
            responseSerializer = NativeRedirectResponse.SERIALIZER,
        )
    }
}
