/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/1/2021.
 */

package com.adyen.checkout.core.common.internal.data.api

import com.adyen.checkout.core.common.internal.api.DispatcherProvider
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.core.common.internal.api.get
import com.adyen.checkout.core.common.internal.data.model.PublicKeyResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class PublicKeyService(
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
