/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getString
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Pass this object to [GooglePayConfiguration.merchantInfo.softwareInfo] to set information about the caller
 * of the API.
 *
 * @param id The id of the client / library making the call.
 * @param version The version of the caller
 */
@Parcelize
data class SoftwareInfo(
    var id: String,
    var version: String,
) : ModelObject() {

    companion object {
        private const val SOFTWARE_ID = "id"
        private const val SOFTWARE_VERSION = "version"

        @JvmField
        val SERIALIZER: Serializer<SoftwareInfo> = object : Serializer<SoftwareInfo> {
            override fun serialize(modelObject: SoftwareInfo): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(SOFTWARE_ID, modelObject.id)
                        putOpt(SOFTWARE_VERSION, modelObject.version)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SoftwareInfo::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = SoftwareInfo(
                id = jsonObject.getString(SOFTWARE_ID),
                version = jsonObject.getString(SOFTWARE_VERSION),
            )
        }
    }
}
