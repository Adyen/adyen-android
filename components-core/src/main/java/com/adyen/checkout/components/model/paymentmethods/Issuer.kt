/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */
package com.adyen.checkout.components.model.paymentmethods

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Issuer(
    var id: String? = null,
    var name: String? = null,
    var isDisabled: Boolean = false,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ID = "id"
        private const val NAME = "name"
        private const val DISABLED = "disabled"

        @JvmField
        val CREATOR = Creator(Issuer::class.java)

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
                    throw ModelSerializationException(PaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Issuer {
                return Issuer(
                    id = jsonObject.getStringOrNull(ID),
                    name = jsonObject.getStringOrNull(NAME),
                    isDisabled = jsonObject.optBoolean(DISABLED, false),
                )
            }
        }
    }
}
