/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/4/2021.
 */

package com.adyen.checkout.adyen3ds2.internal.data.api

import com.adyen.checkout.adyen3ds2.internal.data.model.SubmitFingerprintRequest
import com.adyen.checkout.adyen3ds2.internal.data.model.SubmitFingerprintResponse
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.api.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SubmitFingerprintService(
    private val httpClient: HttpClient,
) {

    suspend fun submitFingerprint(
        request: SubmitFingerprintRequest,
        clientKey: String
    ): SubmitFingerprintResponse = withContext(Dispatchers.IO) {
        httpClient.post(
            path = "v1/submitThreeDS2Fingerprint",
            queryParameters = mapOf("token" to clientKey),
            body = request,
            requestSerializer = SubmitFingerprintRequest.SERIALIZER,
            responseSerializer = SubmitFingerprintResponse.SERIALIZER
        )
    }
}
