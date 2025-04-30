/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.internal.data.model

import android.os.Parcelable
import androidx.annotation.RestrictTo
import org.json.JSONObject

/**
 * Base class for a Model object. A model object is a representation of a JSON response or input from the Checkout API.
 * All model objects can be serialized and deserialized to and from a JSONObject using the [Serializer] interface.
 * All model object also implement Parcelable to be sent as part of Extras in an Intent Bundle.
 *
 * The classes extending [ModelObject] are data classes designed to work standalone or in association with JSON
 * libraries like GSON and Moshi.
 */
abstract class ModelObject
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : Parcelable {

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    /**
     * Interface that must be implemented and provided as a public SERIALIZER field that serializes the to and from a
     * JSONObject.
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
}
