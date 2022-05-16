/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.model

import android.os.Parcel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Collections

private const val INDENTATION_SPACES = 4
private const val PARSING_ERROR = "PARSING_ERROR"
private const val FLAG_NULL = 0
private const val FLAG_NON_NULL = FLAG_NULL + 1

fun JSONObject.getStringOrNull(key: String): String? {
    return if (has(key)) getString(key) else null
}

fun JSONObject.getBooleanOrNull(key: String): Boolean? {
    return if (has(key)) getBoolean(key) else null
}

fun JSONObject.toStringPretty(): String {
    return try {
        toString(INDENTATION_SPACES)
    } catch (e: JSONException) {
        PARSING_ERROR
    }
}

fun JSONObject.optStringList(key: String): List<String>? {
    return JsonUtils.parseOptStringList(optJSONArray(key))
}

fun JSONArray.toStringPretty(): String {
    return try {
        toString(INDENTATION_SPACES)
    } catch (e: JSONException) {
        PARSING_ERROR
    }
}

object JsonUtils {

    /**
     * Writes a [JSONObject] to a [Parcel] as a [String].
     *
     * @param parcel The Parcel to be written to.
     * @param jsonObject The JSONObject to be saved in the Parcel.
     */
    @JvmStatic
    fun writeToParcel(parcel: Parcel, jsonObject: JSONObject?) {
        if (jsonObject == null) {
            parcel.writeInt(FLAG_NULL)
        } else {
            parcel.writeInt(FLAG_NON_NULL)
            parcel.writeString(jsonObject.toString())
        }
    }

    /**
     * Reads a [JSONObject] previously saved on a [Parcel].
     *
     * @param parcel The Parcel to be read.
     * @return The JSONObject that was contained in the Parcel.
     */
    @JvmStatic
    @Throws(JSONException::class)
    fun readFromParcel(parcel: Parcel): JSONObject? {
        return when (parcel.readInt()) {
            FLAG_NULL -> null
            FLAG_NON_NULL -> JSONObject(parcel.readString() ?: "")
            else -> throw IllegalArgumentException("Invalid flag.")
        }
    }

    /**
     * Parses a [JSONArray] to a list of Strings.
     *
     * @param jsonArray The JSONArray to be read.
     * @return A [List] of strings, or null if the jsonArray was null.
     */
    @JvmStatic
    fun parseOptStringList(jsonArray: JSONArray?): List<String>? {
        if (jsonArray == null) {
            return null
        }
        val list: MutableList<String> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.optString(i, null)
            if (item != null) {
                list.add(item)
            }
        }
        return Collections.unmodifiableList(list)
    }

    /**
     * Serializes a List of String to a [JSONArray].
     *
     * @param stringList The [List] of Strings to be serialized.
     * @return The populated [JSONArray]. Could be null.
     */
    @JvmStatic
    fun serializeOptStringList(stringList: List<String?>?): JSONArray? {
        if (stringList == null) {
            return null
        }
        return JSONArray().apply {
            stringList.filter { !it.isNullOrEmpty() }.forEach {
                put(it)
            }
        }
    }
}
