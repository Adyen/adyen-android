/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.common.internal.model

import androidx.annotation.RestrictTo
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ModelUtils {

    /**
     * Parse a [JSONObject] to a class that extends [ModelObject] using its [ModelObject.Serializer].
     * Result can also be null if the object is null.
     *
     * @param jsonObject The object to be parsed.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be parse to.
     * @return The parsed object from JSON, null if doesn't exist.
     */
    @JvmStatic
    fun <T : ModelObject> deserializeOpt(jsonObject: JSONObject?, serializer: ModelObject.Serializer<T>): T? {
        return if (jsonObject == null) null else serializer.deserialize(jsonObject)
    }

    /**
     * Parse a [JSONArray] to a [List] of objects that extend [ModelObject].
     * Result can also be null if the object is null.
     *
     * @param jsonArray The JSONArray to be parsed.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be parse to.
     * @return The List of objects from the JSONArray.
     */
    @JvmStatic
    fun <T : ModelObject> deserializeOptList(jsonArray: JSONArray?, serializer: ModelObject.Serializer<T>): List<T>? {
        if (jsonArray == null) {
            return null
        }
        val list: MutableList<T> = ArrayList()
        for (i in 0 until jsonArray.length()) {
            val itemJson = jsonArray.optJSONObject(i)
            if (itemJson != null) {
                val item = serializer.deserialize(itemJson)
                list.add(item)
            }
        }
        return Collections.unmodifiableList(list)
    }

    /**
     * Parses a [JSONObject] to a [Map] of objects that extend [ModelObject].
     * The result can be null if the input JSONObject is null.
     *
     * @param jsonObject The JSONObject to be parsed.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type of the ModelObject class to be parsed.
     * @return The Map of objects from the JSONObject, or null.
     */
    @JvmStatic
    fun <T : ModelObject> deserializeOptMap(
        jsonObject: JSONObject?,
        serializer: ModelObject.Serializer<T>
    ): Map<String, T?>? {
        if (jsonObject == null) {
            return null
        }
        val map = jsonObject.jsonToMap(serializer)

        return Collections.unmodifiableMap(map)
    }

    /**
     * Serializes a class extending [ModelObject] into a JSONObject.
     *
     * @param modelObject The object to be serialized.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be serialized from.
     * @return The JSONObject representing the ModelObject.
     */
    @JvmStatic
    fun <T : ModelObject> serializeOpt(modelObject: T?, serializer: ModelObject.Serializer<T>): JSONObject? {
        return if (modelObject == null) null else serializer.serialize(modelObject)
    }

    /**
     * Serializes a [List] containing objects that extend [ModelObject] into a [JSONArray].
     *
     * @param modelList The list to be serialized.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be serialized from.
     * @return The JSONArray representing the list of ModelObjects.
     */
    @JvmStatic
    fun <T : ModelObject> serializeOptList(modelList: List<T>?, serializer: ModelObject.Serializer<T>): JSONArray? {
        if (modelList == null || modelList.isEmpty()) {
            return null
        }
        val jsonArray = JSONArray()
        for (model in modelList) {
            jsonArray.put(serializer.serialize(model))
        }
        return jsonArray
    }

    /**
     * Serializes a [Map] where the values are objects that extend [ModelObject] into a [JSONObject].
     *
     * @param modelMap The map to be serialized.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type of the ModelObject class to be serialized from.
     * @return The JSONObject representing the map of ModelObjects.
     */
    @JvmStatic
    fun <T : ModelObject> serializeOptMap(
        modelMap: Map<String, T?>?,
        serializer: ModelObject.Serializer<T>
    ): JSONObject? {
        if (modelMap == null) {
            return null
        }
        return JSONObject().apply {
            modelMap.forEach { (key, value) ->
                put(key, serializeOpt(value, serializer))
            }
        }
    }
}
