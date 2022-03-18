/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/3/2022.
 */
package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class Address(
    var city: String? = null,
    var country: String? = null,
    var houseNumberOrName: String? = null,
    var postalCode: String? = null,
    var stateOrProvince: String? = null,
    var street: String? = null,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val CITY = "city"
        private const val COUNTRY = "country"
        private const val HOUSE_NUMBER_OR_NAME = "houseNumberOrName"
        private const val POSTAL_CODE = "postalCode"
        private const val STATE_OR_PROVINCE = "stateOrProvince"
        private const val STREET = "street"
        const val ADDRESS_NULL_PLACEHOLDER = "null"
        const val ADDRESS_COUNTRY_NULL_PLACEHOLDER = "ZZ"

        @JvmField
        val CREATOR = Creator(Address::class.java)

        @JvmField
        val SERIALIZER: Serializer<Address> = object : Serializer<Address> {
            override fun serialize(modelObject: Address): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(CITY, modelObject.city)
                        putOpt(COUNTRY, modelObject.country)
                        putOpt(HOUSE_NUMBER_OR_NAME, modelObject.houseNumberOrName)
                        putOpt(POSTAL_CODE, modelObject.postalCode)
                        putOpt(STATE_OR_PROVINCE, modelObject.stateOrProvince)
                        putOpt(STREET, modelObject.street)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(Address::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): Address {
                return Address(
                    city = jsonObject.getStringOrNull(CITY),
                    country = jsonObject.getStringOrNull(COUNTRY),
                    houseNumberOrName = jsonObject.getStringOrNull(HOUSE_NUMBER_OR_NAME),
                    postalCode = jsonObject.getStringOrNull(POSTAL_CODE),
                    stateOrProvince = jsonObject.getStringOrNull(STATE_OR_PROVINCE),
                    street = jsonObject.getStringOrNull(STREET),
                )
            }
        }
    }
}
