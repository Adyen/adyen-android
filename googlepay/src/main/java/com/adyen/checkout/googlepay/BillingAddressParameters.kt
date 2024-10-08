/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/7/2019.
 */
package com.adyen.checkout.googlepay

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.getBooleanOrNull
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Pass this object to [GooglePayConfiguration.billingAddressParameters] to set additional fields to be returned for
 * the requested billing address. This object is a mapping of the
 * [BillingAddressParameters](https://developers.google.com/pay/api/android/reference/request-objects#BillingAddressParameters)
 * object from the Google Pay SDK.
 *
 * @param format The format of the billing address. Check the Google Pay SDK documentation for the possible values.
 * @param isPhoneNumberRequired Set to true if a phone number is required for the billing address.
 */
@Suppress("MaxLineLength")
@Parcelize
data class BillingAddressParameters(
    var format: String? = null,
    var isPhoneNumberRequired: Boolean = false,
) : ModelObject() {

    companion object {
        private const val FORMAT = "format"
        private const val PHONE_NUMBER_REQUIRED = "phoneNumberRequired"

        @JvmField
        val SERIALIZER: Serializer<BillingAddressParameters> = object : Serializer<BillingAddressParameters> {
            override fun serialize(modelObject: BillingAddressParameters): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(FORMAT, modelObject.format)
                        putOpt(PHONE_NUMBER_REQUIRED, modelObject.isPhoneNumberRequired)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(BillingAddressParameters::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject) = BillingAddressParameters(
                format = jsonObject.getStringOrNull(FORMAT),
                isPhoneNumberRequired = jsonObject.getBooleanOrNull(PHONE_NUMBER_REQUIRED) ?: false,
            )
        }
    }
}
