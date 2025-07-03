/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 12/5/2025.
 */

package com.adyen.checkout.core.components.data

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class Address(
    var city: String? = null,
    var country: String? = null,
    var houseNumberOrName: String? = null,
    var postalCode: String? = null,
    var stateOrProvince: String? = null,
    var street: String? = null,
) : ModelObject() {

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
