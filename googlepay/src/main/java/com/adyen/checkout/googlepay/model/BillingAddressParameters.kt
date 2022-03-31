/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/7/2019.
 */
package com.adyen.checkout.googlepay.model

import android.os.Parcel
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils.writeToParcel
import com.adyen.checkout.core.model.ModelObject
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class BillingAddressParameters(
    var format: String? = null,
    var isPhoneNumberRequired: Boolean = false,
) : ModelObject() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        writeToParcel(dest, SERIALIZER.serialize(this))
    }

    companion object {
        private const val FORMAT = "format"
        private const val PHONE_NUMBER_REQUIRED = "phoneNumberRequired"

        @JvmField
        val CREATOR = Creator(BillingAddressParameters::class.java)

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
