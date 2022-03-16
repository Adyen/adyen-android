/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.model

import com.adyen.checkout.core.exception.BadModelException
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Modifier
import java.util.Collections

object ModelUtils {
    const val SERIALIZER_FIELD_NAME = "SERIALIZER"

    /**
     * Parse a [JSONObject] to a class that extends [ModelObject] using its [ModelObject.Serializer].
     *
     * @param jsonObject The object to be parsed.
     * @param modelClass The class type to be parsed to.
     * @param <T> The type o the ModelObject class to be parse to.
     * @return The parsed object.
     */
    @JvmStatic
    fun <T : ModelObject> deserializeModel(jsonObject: JSONObject, modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        val serializer = readModelSerializer(modelClass) as ModelObject.Serializer<T>
        return serializer.deserialize(jsonObject)
    }

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

    @Suppress("ThrowsCount")
    private fun readModelSerializer(modelClass: Class<*>): ModelObject.Serializer<*> {
        // TODO cache previous Serializers in HashMap?
        return try {
            val field = modelClass.getField(SERIALIZER_FIELD_NAME)
            if (field.modifiers and Modifier.STATIC == 0) {
                // Field is not static
                throw BadModelException(modelClass, null)
            }
            if (!ModelObject.Serializer::class.java.isAssignableFrom(field.type)) {
                // SERIALIZER field is not of type Serializer
                throw BadModelException(modelClass, null)
            }
            field[null] as ModelObject.Serializer<*>
        } catch (e: NoSuchFieldException) {
            throw BadModelException(modelClass, e)
        } catch (e: IllegalAccessException) {
            throw BadModelException(modelClass, e)
        }
    }
}
