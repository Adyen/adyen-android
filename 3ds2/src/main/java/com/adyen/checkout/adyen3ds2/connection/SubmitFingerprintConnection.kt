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

class SubmitFingerprintConnection(
    private val request: SubmitFingerprintRequest,
    environment: Environment,
    clientKey: String
) : Connection<SubmitFingerprintResponse>(environment.baseUrl) {

    private val path = "v1/submitThreeDS2Fingerprint?token=$clientKey"

    @Throws(IOException::class, JSONException::class)
    override fun call(): SubmitFingerprintResponse {
        Logger.v(TAG, "call - $path")
        val requestJson = SubmitFingerprintRequest.SERIALIZER.serialize(request)
        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")
        val result = post(path, requestJson.toString(), CONTENT_TYPE_JSON_HEADER)
        val resultJson = JSONObject(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return SubmitFingerprintResponse.SERIALIZER.deserialize(resultJson)
    }
}
