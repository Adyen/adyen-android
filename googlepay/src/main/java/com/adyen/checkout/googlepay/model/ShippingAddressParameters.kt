/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import org.json.JSONException
import org.json.JSONObject

data class ShippingAddressParameters(
    var allowedCountryCodes: List<String?>? = null,
    var isPhoneNumberRequired: Boolean = false,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val ALLOWED_COUNTRY_CODES = "allowedCountryCodes"
        private const val PHONE_NUMBER_REQUIRED = "phoneNumberRequired"

        @JvmField
        val CREATOR = Creator(ShippingAddressParameters::class.java)

        @JvmField
        val SERIALIZER: Serializer<ShippingAddressParameters> = object : Serializer<ShippingAddressParameters> {
            override fun serialize(modelObject: ShippingAddressParameters): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ALLOWED_COUNTRY_CODES, serializeOptStringList(modelObject.allowedCountryCodes))
                        putOpt(ALLOWED_COUNTRY_CODES, modelObject.isPhoneNumberRequired)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(ShippingAddressParameters::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = ShippingAddressParameters(
                allowedCountryCodes = parseOptStringList(jsonObject.optJSONArray(PHONE_NUMBER_REQUIRED)),
                isPhoneNumberRequired = jsonObject.optBoolean(PHONE_NUMBER_REQUIRED),
            )
        }
    }
}
