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
import com.adyen.checkout.core.api.Connection.Companion.CONTENT_TYPE_JSON_HEADER
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val TAG = LogUtil.getTag()

internal class BinLookupService(
    private val request: BinLookupRequest,
    private val environment: Environment,
    clientKey: String
) {

    private val path = "v2/bin/binLookup?clientKey=$clientKey"

    suspend fun makeBinLookup(): BinLookupResponse = withContext(Dispatchers.IO) {
        Logger.v(TAG, "call - $path")

        val requestJson = BinLookupRequest.SERIALIZER.serialize(request)

        Logger.v(TAG, "request - ${requestJson.toStringPretty()}")

        val httpClient = HttpClientFactory.getHttpClient(environment.baseUrl)
        val result = httpClient.post(path, requestJson.toString(), CONTENT_TYPE_JSON_HEADER)
        val resultJson = JSONObject(String(result, Charsets.UTF_8))

        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")

        BinLookupResponse.SERIALIZER.deserialize(resultJson)
    }
}
