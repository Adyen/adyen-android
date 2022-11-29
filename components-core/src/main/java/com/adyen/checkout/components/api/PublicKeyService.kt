/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/1/2021.
 */

package com.adyen.checkout.components.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.PublicKeyResponse
import com.adyen.checkout.core.api.HttpClient
import com.adyen.checkout.core.api.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PublicKeyService(
    private val httpClient: HttpClient,
) {

    suspend fun getPublicKey(
        clientKey: String
    ): PublicKeyResponse = withContext(Dispatchers.IO) {
        httpClient.get(
            "v1/clientKeys/$clientKey",
            PublicKeyResponse.SERIALIZER
        )
    }
}
