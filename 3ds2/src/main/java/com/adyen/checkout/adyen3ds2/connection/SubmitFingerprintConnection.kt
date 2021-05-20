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
import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

private val TAG = LogUtil.getTag()
private const val ENDPOINT = "v1/submitThreeDS2Fingerprint?token="

class SubmitFingerprintConnection(
    private val request: SubmitFingerprintRequest,
    environment: Environment,
    clientKey: String
) : Connection<SubmitFingerprintResponse>(
    "${environment.baseUrl}$ENDPOINT$clientKey"
) {
    @Throws(IOException::class, JSONException::class)
    override fun call(): SubmitFingerprintResponse {
        Logger.v(TAG, "call - $url")
        val requestJson = SubmitFingerprintRequest.SERIALIZER.serialize(request)
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")
        val result = post(CONTENT_TYPE_JSON_HEADER, requestJson.toString().toByteArray(Charsets.UTF_8))
        val resultJson = JSONObject(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return SubmitFingerprintResponse.SERIALIZER.deserialize(resultJson)
    }
}
