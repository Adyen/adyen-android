/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.CheckoutException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;

/**
 * Base class for a Model object. A model object is a representation of a JSON response or input from the Checkout API.
 * All model objects can be serialized and deserialized to and from a JSONObject using the {@link Serializer} interface.
 * All model object also implement Parcelable to be sent as part of Extras in an Intent Bundle.
 *
 * <p/>
 * The classes extending {@link ModelObject} are data classes designed to work standalone or in association with JSON libraries like GSON and Moshi.
 */
public abstract class ModelObject implements Parcelable {

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public final int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    /**
     * Interface that must be implemented and provided as a public SERIALIZER field that serializes the to and from a JSONObject.
     * @param <T> The class that extends ModelObject to be serialized.
     */
    public interface Serializer<T extends ModelObject> {

        /**
         * Serialize the {@link ModelObject} to a {@link JSONObject}.
         * @param modelObject The Model class to be serialized.
         * @return The result JSONObject
         */
        @NonNull
        JSONObject serialize(@NonNull T modelObject);

        /**
         * Deserialize a {@link JSONObject} to a {@link ModelObject}.
         * @param jsonObject The base object to deserialize.
         * @return The ModelObject parsed with the contents from the JSONObject.
         */
        @NonNull
        T deserialize(@NonNull JSONObject jsonObject);
    }

    /**
     * A helper class that implements the Parcelable.Creator for a ModelObject.
     * @param <T> The specific class that extends the ModelObject.
     */
    public static class Creator<T extends ModelObject> implements Parcelable.Creator<T> {
        private final Class<T> mClass;

        public Creator(@NonNull Class<T> clazz) {
            mClass = clazz;
        }

        @NonNull
        @Override
        public final T createFromParcel(@NonNull Parcel source) {
            final JSONObject jsonObject;
            try {
                jsonObject = JsonUtils.readFromParcel(source);
                if (jsonObject == null) {
                    throw new CheckoutException("Failed to create ModelObject from parcel. JSONObject is null.");
                }
            } catch (JSONException e) {
                throw new CheckoutException("Failed to create ModelObject from parcel.", e);
            }

            return ModelUtils.deserializeModel(jsonObject, mClass);
        }

        @NonNull
        @Override
        public T[] newArray(int size) {
            //noinspection unchecked
            return (T[]) Array.newInstance(mClass, size);
        }
    }
}
