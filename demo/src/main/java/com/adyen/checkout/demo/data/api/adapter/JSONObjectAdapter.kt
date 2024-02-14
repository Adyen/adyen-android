/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.data.api.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject

class JSONObjectAdapter {

    @Suppress("UNCHECKED_CAST", "SwallowedException")
    @FromJson
    fun fromJson(reader: JsonReader): JSONObject? {
        return (reader.readJsonValue() as? Map<String, Any>)?.let { data ->
            try {
                JSONObject(data)
            } catch (e: JSONException) {
                JSONObject()
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONObject?) {
        value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
    }
}
