/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */
package com.adyen.checkout.googlepay

import com.adyen.checkout.core.common.internal.model.JsonUtils.parseOptStringList
import com.adyen.checkout.core.common.internal.model.JsonUtils.serializeOptStringList
import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.getBooleanOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Pass this object to [GooglePayConfiguration.shippingAddressParameters] to the required shipping address details.
 * This object is a mapping of the
 * [ShippingAddressParameters](https://developers.google.com/pay/api/android/reference/request-objects#ShippingAddressParameters)
 * object from the Google Pay SDK.
 *
 * @param allowedCountryCodes A list of ISO 3166-1 alpha-2 country code values where shipping is allowed.
 * @param isPhoneNumberRequired Set to true if a phone number is required for the shipping address.
 */
@Suppress("MaxLineLength")
@Parcelize
data class ShippingAddressParameters(
    var allowedCountryCodes: List<String?>? = null,
    var isPhoneNumberRequired: Boolean = false,
) : ModelObject() {

    companion object {
        private const val ALLOWED_COUNTRY_CODES = "allowedCountryCodes"
        private const val PHONE_NUMBER_REQUIRED = "phoneNumberRequired"

        @JvmField
        val SERIALIZER: Serializer<ShippingAddressParameters> = object : Serializer<ShippingAddressParameters> {
            @Suppress("TooGenericExceptionThrown")
            override fun serialize(modelObject: ShippingAddressParameters): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(ALLOWED_COUNTRY_CODES, serializeOptStringList(modelObject.allowedCountryCodes))
                        putOpt(PHONE_NUMBER_REQUIRED, modelObject.isPhoneNumberRequired)
                    }
                } catch (e: JSONException) {
                    // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
//                    throw ModelSerializationException(ShippingAddressParameters::class.java, e)
                    throw RuntimeException(e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = ShippingAddressParameters(
                allowedCountryCodes = parseOptStringList(jsonObject.optJSONArray(ALLOWED_COUNTRY_CODES)),
                isPhoneNumberRequired = jsonObject.getBooleanOrNull(PHONE_NUMBER_REQUIRED) ?: false,
            )
        }
    }
}
