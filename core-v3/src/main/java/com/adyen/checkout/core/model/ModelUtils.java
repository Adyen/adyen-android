/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/4/2019.
 */

package com.adyen.checkout.core.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.BadModelException;
import com.adyen.checkout.core.exception.NoConstructorException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModelUtils {

    public static final String SERIALIZER_FIELD_NAME = "SERIALIZER";

    /**
     * Parse a {@link JSONObject} to a class that extends {@link ModelObject} using its {@link com.adyen.checkout.core.model.ModelObject.Serializer}.
     *
     * @param jsonObject The object to be parsed.
     * @param modelClass The class type to be parsed to.
     * @param <T> The type o the ModelObject class to be parse to.
     * @return The parsed object.
     */
    @NonNull
    static <T extends ModelObject> T deserializeModel(@NonNull JSONObject jsonObject, @NonNull Class<T> modelClass) {

        //noinspection unchecked
        final ModelObject.Serializer<T> serializer = (ModelObject.Serializer<T>) ModelUtils.readModelSerializer(modelClass);

        return serializer.deserialize(jsonObject);
    }

    /**
     * Parse a {@link JSONObject} to a class that extends {@link ModelObject} using its {@link com.adyen.checkout.core.model.ModelObject.Serializer}.
     * Result can also be null if the object is null.
     *
     * @param jsonObject The object to be parsed.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be parse to.
     * @return The parsed object from JSON, null if doesn't exist.
     */
    @Nullable
    public static <T extends ModelObject> T deserializeOpt(@Nullable JSONObject jsonObject, @NonNull ModelObject.Serializer<T> serializer) {
        return jsonObject == null ? null : serializer.deserialize(jsonObject);
    }

    /**
     * Parse a {@link JSONArray} to a {@link List} of objects that extend {@link ModelObject}.
     * Result can also be null if the object is null.
     *
     * @param jsonArray The JSONArray to be parsed.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be parse to.
     * @return
     */
    @Nullable
    public static <T extends ModelObject> List<T> deserializeOptList(@Nullable JSONArray jsonArray, @NonNull ModelObject.Serializer<T> serializer) {
        if (jsonArray == null) {
            return null;
        }

        final List<T> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject itemJson = jsonArray.optJSONObject(i);
            if (itemJson != null) {
                final T item = serializer.deserialize(itemJson);
                list.add(item);
            }
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Serializes a class extending {@link ModelObject} into a JSONObject.
     *
     * @param modelObject The object to be serialized.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be serialized from.
     * @return The JSONObject representing the ModelObject.
     */
    @Nullable
    public static <T extends ModelObject> JSONObject serializeOpt(@Nullable T modelObject, @NonNull ModelObject.Serializer<T> serializer) {
        return modelObject == null ? null : serializer.serialize(modelObject);
    }

    /**
     * Serializes a {@link List} containing objects that extend {@link ModelObject} into a {@link JSONArray}.
     *
     * @param modelList The list to be serialized.
     * @param serializer The serializer of the ModelObject class to be used.
     * @param <T> The type o the ModelObject class to be serialized from.
     * @return The JSONArray representing the list of ModelObjects.
     */
    @Nullable
    public static <T extends ModelObject> JSONArray serializeOptList(@Nullable List<T> modelList, @NonNull ModelObject.Serializer<T> serializer) {
        if (modelList == null || modelList.isEmpty()) {
            return null;
        }

        final JSONArray jsonArray = new JSONArray();
        for (T model : modelList) {
            jsonArray.put(serializer.serialize(model));
        }

        return jsonArray;
    }

    private static ModelObject.Serializer<?> readModelSerializer(Class<?> modelClass) {
        // TODO cache previous Serializers in HashMap?
        try {
            final Field field = modelClass.getField(SERIALIZER_FIELD_NAME);

            if ((field.getModifiers() & Modifier.STATIC) == 0) {
                // Field is not static
                throw new BadModelException(modelClass, null);
            }
            if (!ModelObject.Serializer.class.isAssignableFrom(field.getType())) {
                // SERIALIZER field is not of type Serializer
                throw new BadModelException(modelClass, null);
            }

            return (ModelObject.Serializer<?>) field.get(null);

        } catch (NoSuchFieldException e) {
            throw new BadModelException(modelClass, e);
        } catch (IllegalAccessException e) {
            throw new BadModelException(modelClass, e);
        }
    }

    private ModelUtils() {
        throw new NoConstructorException();
    }
}
