/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/4/2021.
 */

package com.adyen.checkout.adyen3ds2.connection

import com.adyen.checkout.adyen3ds2.model.SubmitFingerprintRequest
import com.adyen.checkout.adyen3ds2.model.SubmitFingerprintResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.api.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SubmitFingerprintService(
    private val environment: Environment,
) {

    suspend fun submitFingerprint(
        request: SubmitFingerprintRequest,
        clientKey: String
    ): SubmitFingerprintResponse = withContext(Dispatchers.IO) {
        HttpClientFactory.getHttpClient(environment.baseUrl).post(
            path = "v1/submitThreeDS2Fingerprint?token=$clientKey",
            body = request,
            requestSerializer = SubmitFingerprintRequest.SERIALIZER,
            responseSerializer = SubmitFingerprintResponse.SERIALIZER
        )
    }
}
