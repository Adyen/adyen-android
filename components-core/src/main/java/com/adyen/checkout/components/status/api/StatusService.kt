/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */
package com.adyen.checkout.components.status.api

import com.adyen.checkout.components.status.model.StatusRequest
import com.adyen.checkout.components.status.model.StatusResponse
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.api.post

class StatusService(
    private val baseUrl: String
) {

    fun checkStatus(
        clientKey: String,
        statusRequest: StatusRequest
    ): StatusResponse {
        return HttpClientFactory.getHttpClient(baseUrl).post(
            path = "services/PaymentInitiation/v1/status?token=$clientKey",
            body = statusRequest,
            requestSerializer = StatusRequest.SERIALIZER,
            responseSerializer = StatusResponse.SERIALIZER
        )
    }
}
