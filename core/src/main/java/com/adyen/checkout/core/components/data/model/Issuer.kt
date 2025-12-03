/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */
package com.adyen.checkout.core.components.data.model

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Issuer(
    var id: String? = null,
    var name: String? = null,
    var isDisabled: Boolean = false,
) : ModelObject() {

    companion object {
        private const val ID = "id"
        private const val NAME = "name"
        private const val DISABLED = "disabled"

        @JvmField
        val SERIALIZER: Serializer<Issuer> = object : Serializer<Issuer> {
            override fun serialize(modelObject: Issuer): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ID, modelObject.id)
                        putOpt(NAME, modelObject.name)
                        putOpt(DISABLED, modelObject.isDisabled)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Issuer::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Issuer {
                return Issuer(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                    isDisabled = jsonObject.getBooleanOrNull(DISABLED) ?: false,
                )
            }
        }
    }
}
