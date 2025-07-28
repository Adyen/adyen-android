/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/7/2025.
 */
package com.adyen.checkout.core.components.internal.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.api.HttpClient
import com.adyen.checkout.core.common.internal.api.post
import com.adyen.checkout.core.components.internal.data.model.StatusRequest
import com.adyen.checkout.core.components.internal.data.model.StatusResponse

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class StatusService(
    private val httpClient: HttpClient,
) {

    internal suspend fun checkStatus(
        clientKey: String,
        statusRequest: StatusRequest
    ): StatusResponse {
        return httpClient.post(
            path = "services/PaymentInitiation/v1/status",
            queryParameters = mapOf("token" to clientKey),
            body = statusRequest,
            requestSerializer = StatusRequest.SERIALIZER,
            responseSerializer = StatusResponse.SERIALIZER,
        )
    }
}
