/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/9/2025.
 */

package com.adyen.checkout.googlepay.old

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Holds information about the caller of the API.
 *
 * @param id The id of the client / library making the call.
 * @param version The version of the caller
 */
@Parcelize
data class SoftwareInfo(
    val id: String,
    val version: String,
) : ModelObject() {

    companion object {
        private const val SOFTWARE_ID = "id"
        private const val SOFTWARE_VERSION = "version"

        @JvmField
        val SERIALIZER: Serializer<SoftwareInfo> = object : Serializer<SoftwareInfo> {
            override fun serialize(modelObject: SoftwareInfo): JSONObject {
                return try {
                    JSONObject().apply {
                        put(SOFTWARE_ID, modelObject.id)
                        put(SOFTWARE_VERSION, modelObject.version)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SoftwareInfo::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = try {
                SoftwareInfo(
                    id = jsonObject.getString(SOFTWARE_ID),
                    version = jsonObject.getString(SOFTWARE_VERSION),
                )
            } catch (e: JSONException) {
                throw ModelSerializationException(SoftwareInfo::class.java, e)
            }
        }
    }
}
