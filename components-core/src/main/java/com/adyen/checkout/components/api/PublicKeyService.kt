/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/1/2021.
 */

package com.adyen.checkout.components.api

import com.adyen.checkout.components.model.PublicKeyResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.api.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PublicKeyService(
    private val environment: Environment
) {

    suspend fun getPublicKey(
        clientKey: String
    ): PublicKeyResponse = withContext(Dispatchers.IO) {
        HttpClientFactory.getHttpClient(environment.baseUrl).get(
            "v1/clientKeys/$clientKey",
            PublicKeyResponse.SERIALIZER
        )
    }
}
