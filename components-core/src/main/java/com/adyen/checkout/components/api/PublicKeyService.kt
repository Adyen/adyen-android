/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/1/2021.
 */

package com.adyen.checkout.components.api

import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.api.HttpClientFactory
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val TAG = LogUtil.getTag()
private const val PUBLIC_KEY_JSON_KEY = "publicKey"

internal class PublicKeyService(
    private val environment: Environment,
    clientKey: String
) {

    private val path = "v1/clientKeys/$clientKey"

    suspend fun getPublicKey(): String = withContext(Dispatchers.IO) {
        Logger.v(TAG, "call - $path")

        val httpClient = HttpClientFactory.getHttpClient(environment.baseUrl)
        val result = String(httpClient.get(path), Charsets.UTF_8)
        val jsonObject = JSONObject(result)

        Logger.v(TAG, "result: $result")

        jsonObject.getString(PUBLIC_KEY_JSON_KEY)
    }
}
