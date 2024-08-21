/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
@file:Suppress("TooManyFunctions")

package com.adyen.checkout.core.internal.data.model

import androidx.annotation.RestrictTo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Collections

private const val INDENTATION_SPACES = 4
private const val PARSING_ERROR = "PARSING_ERROR"

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getStringOrNull(key: String): String? {
    return if (has(key)) getString(key) else null
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getBooleanOrNull(key: String): Boolean? {
    return if (has(key)) getBoolean(key) else null
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getIntOrNull(key: String): Int? {
    return if (has(key)) getInt(key) else null
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getLongOrNull(key: String): Long? {
    return if (has(key)) getLong(key) else null
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.toStringPretty(): String {
    @Suppress("SwallowedException")
    return try {
        toString(INDENTATION_SPACES)
    } catch (e: JSONException) {
        PARSING_ERROR
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.optStringList(key: String): List<String>? {
    return JsonUtils.parseOptStringList(optJSONArray(key))
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.optIntList(key: String): List<Int>? {
    return JsonUtils.parseOptIntegerList(optJSONArray(key))
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONArray.toStringPretty(): String {
    @Suppress("SwallowedException")
    return try {
        toString(INDENTATION_SPACES)
    } catch (e: JSONException) {
        PARSING_ERROR
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Throws(JSONException::class)
inline fun <reified T : ModelObject> JSONObject.jsonToMap(
    modelSerializer: ModelObject.Serializer<T>
): Map<String, T?> {
    val map = mutableMapOf<String, T?>()

    if (this !== JSONObject.NULL) {
        val keysItr = this.keys()

        while (keysItr.hasNext()) {
            val key = keysItr.next()
            val value = this[key]

            if (value is JSONObject) {
                map[key] = ModelUtils.deserializeOpt(value, modelSerializer)
            }
        }
    }

    return map
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getMapOrNull(key: String): Map<String, String>? {
    return if (has(key)) getJSONObject(key).toMap() else null
}

private fun JSONObject.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()

    val iterator = keys()
    while (iterator.hasNext()) {
        val key = iterator.next()
        val value = this[key]

        if (value is String) {
            map[key] = value
        }
    }

    return map
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object JsonUtils {

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

    /**
     * Parses a [JSONArray] to a list of integers.
     *
     * @param jsonArray The JSONArray to be read.
     * @return A [List] of integers, or null if the jsonArray was null.
     */
    @JvmStatic
    @Throws(JSONException::class)
    fun parseOptIntegerList(jsonArray: JSONArray?): List<Int>? {
        if (jsonArray == null) {
            return null
        }
        val list: MutableList<Int> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val jsonValue = jsonArray.opt(i)
            if (jsonValue is Int) {
                val item = jsonArray.optInt(i)
                list.add(item)
            } else {
                throw JSONException("type is not integer")
            }
        }
        return Collections.unmodifiableList(list)
    }

    /**
     * Serializes a List of integers to a [JSONArray].
     *
     * @param intList The [List] of integers to be serialized.
     * @return The populated [JSONArray]. Could be null.
     */
    @JvmStatic
    fun serializeOptIntegerList(intList: List<Int>?): JSONArray? {
        if (intList == null) {
            return null
        }
        return JSONArray().apply {
            intList.forEach {
                put(it)
            }
        }
    }
}
