/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.model

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.model.JsonUtils.readFromParcel
import com.adyen.checkout.core.model.ModelObject.Serializer
import org.json.JSONException
import org.json.JSONObject

/**
 * Base class for a Model object. A model object is a representation of a JSON response or input from the Checkout API.
 * All model objects can be serialized and deserialized to and from a JSONObject using the [Serializer] interface.
 * All model object also implement Parcelable to be sent as part of Extras in an Intent Bundle.
 *
 * The classes extending [ModelObject] are data classes designed to work standalone or in association with JSON libraries like GSON and Moshi.
 */
abstract class ModelObject : Parcelable {
    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    /**
     * Interface that must be implemented and provided as a public SERIALIZER field that serializes the to and from a JSONObject.
     * @param <T> The class that extends ModelObject to be serialized.
     */
    interface Serializer<T : ModelObject> {
        /**
         * Serialize the [ModelObject] to a [JSONObject].
         * @param modelObject The Model class to be serialized.
         * @return The result JSONObject
         */
        fun serialize(modelObject: T): JSONObject

        /**
         * Deserialize a [JSONObject] to a [ModelObject].
         * @param jsonObject The base object to deserialize.
         * @return The ModelObject parsed with the contents from the JSONObject.
         */
        fun deserialize(jsonObject: JSONObject): T
    }

    /**
     * A helper class that implements the Parcelable.Creator for a ModelObject.
     * @param <T> The specific class that extends the ModelObject.
     */
    class Creator<T : ModelObject>(private val mClass: Class<T>) : Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel): T {
            val jsonObject: JSONObject = try {
                readFromParcel(source)
                    ?: throw CheckoutException("Failed to create ModelObject from parcel. JSONObject is null.")
            } catch (e: JSONException) {
                throw CheckoutException("Failed to create ModelObject from parcel.", e)
            }
            return ModelUtils.deserializeModel(jsonObject, mClass)
        }

        override fun newArray(size: Int): Array<T> {
            @Suppress("UNCHECKED_CAST")
            return java.lang.reflect.Array.newInstance(mClass, size) as Array<T>
        }
    }
}
