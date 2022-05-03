/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 11/3/2022.
 */

package com.adyen.checkout.card.api

import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.core.api.Connection
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.ModelUtils
import com.adyen.checkout.core.model.toStringPretty
import org.json.JSONArray

private val TAG = LogUtil.getTag()
private const val ENDPOINT = "datasets/"
private const val JSON_SUFFIX = ".json"

class AddressConnection(
    environment: Environment,
    dataType: AddressDataType,
    localeString: String,
    countryCode: String?
) : Connection<List<AddressItem>>(makeUrl(environment, dataType, localeString, countryCode)) {

    override fun call(): List<AddressItem> {
        Logger.v(TAG, "call - $url")
        val result = get(CONTENT_TYPE_JSON_HEADER)
        val resultJson = JSONArray(String(result, Charsets.UTF_8))
        Logger.v(TAG, "response: ${resultJson.toStringPretty()}")
        return ModelUtils.deserializeOptList(resultJson, AddressItem.SERIALIZER).orEmpty()
    }
}

fun makeUrl(
    environment: Environment,
    dataType: AddressDataType,
    localeString: String,
    countryCode: String? = null
): String {
    return when (dataType) {
        AddressDataType.COUNTRY -> "${environment.baseUrl}$ENDPOINT${dataType.pathParam}/$localeString$JSON_SUFFIX"
        AddressDataType.STATE -> "${environment.baseUrl}$ENDPOINT${dataType.pathParam}/$countryCode/$localeString$JSON_SUFFIX"
    }
}

enum class AddressDataType(val pathParam: String) {
    COUNTRY("countries"),
    STATE("states")
}
