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
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

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
                isPhoneNumberRequired = jsonObject.optBoolean(PHONE_NUMBER_REQUIRED),
            )
        }
    }
}
