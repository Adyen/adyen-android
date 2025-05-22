/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.internal.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

internal object ModelUtils {

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
}
