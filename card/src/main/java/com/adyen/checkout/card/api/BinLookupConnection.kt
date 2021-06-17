/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/1/2021.
 */

package com.adyen.checkout.card.api

import com.adyen.checkout.card.api.model.BinLookupRequest
import com.adyen.checkout.card.api.model.BinLookupResponse
import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import org.json.JSONObject

private val TAG = LogUtil.getTag()
private const val ENDPOINT = "v2/bin/binLookup?clientKey="

class BinLookupConnection(
    private val request: BinLookupRequest,
    environment: Environment,
    clientKey: String
) : Connection<BinLookupResponse>("${environment.baseUrl}$ENDPOINT$clientKey") {

    override fun call(): BinLookupResponse {
        Logger.v(TAG, "call - $url")
        Logger.v(TAG, "request - ${BinLookupRequest.SERIALIZER.serialize(request).toStringPretty()}")
        val requestString = BinLookupRequest.SERIALIZER.serialize(request).toString()
        val result = post(CONTENT_TYPE_JSON_HEADER, requestString.toByteArray(Charsets.UTF_8))
        val resultJson = JSONObject(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return BinLookupResponse.SERIALIZER.deserialize(resultJson)
    }
}
