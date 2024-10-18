/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/1/2021.
 */

package com.adyen.checkout.components.core.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.model.PublicKeyResponse
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PublicKeyService(
    private val httpClient: HttpClient,
    private val coroutineDispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    internal suspend fun getPublicKey(
        clientKey: String
    ): PublicKeyResponse = withContext(coroutineDispatcher) {
        httpClient.get(
            "v1/clientKeys/$clientKey",
            PublicKeyResponse.SERIALIZER
        )
    }
}
