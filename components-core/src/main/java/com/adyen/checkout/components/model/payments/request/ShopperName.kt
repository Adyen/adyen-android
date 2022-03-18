/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/12/2019.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class ShopperName(
    var firstName: String? = null,
    var infix: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val FIRST_NAME = "firstName"
        private const val INFIX = "infix"
        private const val LAST_NAME = "lastName"
        private const val GENDER = "gender"

        @JvmField
        val CREATOR = Creator(
            ShopperName::class.java
        )

        @JvmField
        val SERIALIZER: Serializer<ShopperName> = object : Serializer<ShopperName> {
            override fun serialize(modelObject: ShopperName): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(FIRST_NAME, modelObject.firstName)
                        putOpt(INFIX, modelObject.infix)
                        putOpt(LAST_NAME, modelObject.lastName)
                        putOpt(GENDER, modelObject.gender)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(ShopperName::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): ShopperName {
                return ShopperName(
                    firstName = jsonObject.getStringOrNull(FIRST_NAME),
                    infix = jsonObject.getStringOrNull(INFIX),
                    lastName = jsonObject.getStringOrNull(LAST_NAME),
                    gender = jsonObject.getStringOrNull(GENDER),
                )
            }
        }
    }
}
