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
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger
import org.json.JSONObject
import java.nio.charset.Charset

internal class StatusService {

    fun checkStatus(
        url: String,
        statusRequest: StatusRequest
    ): StatusResponse {
        Logger.v(TAG, "call - $url")

        val body = StatusRequest.SERIALIZER.serialize(statusRequest).toString()

        val httpClient = HttpClientFactory.getHttpClient(url)
        val bytes = httpClient.post("", body)

        val result = String(bytes, Charset.defaultCharset())
        val jsonObject = JSONObject(result)

        return StatusResponse.SERIALIZER.deserialize(jsonObject)
    }

    companion object {
        private val TAG = getTag()
    }
}
