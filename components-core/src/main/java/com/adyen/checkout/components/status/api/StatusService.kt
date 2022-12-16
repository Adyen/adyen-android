/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */
package com.adyen.checkout.components.status.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.status.model.StatusRequest
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.api.HttpClient
import com.adyen.checkout.core.api.post

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class StatusService(
    private val httpClient: HttpClient,
) {

    suspend fun checkStatus(
        clientKey: String,
        statusRequest: StatusRequest
    ): StatusResponse {
        return httpClient.post(
            path = "services/PaymentInitiation/v1/status",
            queryParameters = mapOf("token" to clientKey),
            body = statusRequest,
            requestSerializer = StatusRequest.SERIALIZER,
            responseSerializer = StatusResponse.SERIALIZER
        )
    }
}
