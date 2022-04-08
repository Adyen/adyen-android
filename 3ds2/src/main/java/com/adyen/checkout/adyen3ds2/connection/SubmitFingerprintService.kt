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
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val TAG = LogUtil.getTag()

internal class SubmitFingerprintService(
    private val environment: Environment,
) {

    suspend fun submitFingerprint(
        request: SubmitFingerprintRequest,
        clientKey: String
    ): SubmitFingerprintResponse = withContext(Dispatchers.IO) {
        val path = "v1/submitThreeDS2Fingerprint?token=$clientKey"

        Logger.v(TAG, "call - $path")

        val requestJson = SubmitFingerprintRequest.SERIALIZER.serialize(request)

        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")

        val httpClient = HttpClientFactory.getHttpClient(environment.baseUrl)
        val result = httpClient.post(path, requestJson.toString())
        val resultJson = JSONObject(String(result, Charsets.UTF_8))

        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")

        SubmitFingerprintResponse.SERIALIZER.deserialize(resultJson)
    }
}
