/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/1/2021.
 */

package com.adyen.checkout.components.api

import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

private val TAG = LogUtil.getTag()
private const val ENDPOINT = "v1/clientKeys/"
private const val PUBLIC_KEY_JSON_KEY = "publicKey"

class PublicKeyConnection(environment: Environment, clientKey: String) : Connection<String>(
    "${environment.baseUrl}$ENDPOINT$clientKey"
) {
    @Throws(IOException::class, JSONException::class)
    override fun call(): String {
        Logger.v(TAG, "call - $url")
        val result = String(get(), Charsets.UTF_8)
        val jsonObject = JSONObject(result)
        Logger.v(TAG, "result: $result")
        return jsonObject.getString(PUBLIC_KEY_JSON_KEY)
    }
}
