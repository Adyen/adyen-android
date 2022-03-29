/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.components.model.payments.request

import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.model.getStringOrNull
import org.json.JSONException
import org.json.JSONObject

data class BacsDirectDebitPaymentMethod(
    override var type: String? = null,
    var holderName: String? = null,
    var bankAccountNumber: String? = null,
    var bankLocationId: String? = null,
) : PaymentMethodDetails() {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        JsonUtils.writeToParcel(parcel, SERIALIZER.serialize(this))
    }

    companion object {
        private const val HOLDER_NAME = "holderName"
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"
        private const val BANK_LOCATION_ID = "bankLocationId"

        const val PAYMENT_METHOD_TYPE = "directdebit_GB"

        @JvmField
        val CREATOR: Parcelable.Creator<BacsDirectDebitPaymentMethod> =
            Creator(BacsDirectDebitPaymentMethod::class.java)

        @JvmField
        val SERIALIZER: Serializer<BacsDirectDebitPaymentMethod> = object : Serializer<BacsDirectDebitPaymentMethod> {

            override fun serialize(modelObject: BacsDirectDebitPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(HOLDER_NAME, modelObject.holderName)
                        putOpt(BANK_ACCOUNT_NUMBER, modelObject.bankAccountNumber)
                        putOpt(BANK_LOCATION_ID, modelObject.bankLocationId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(BacsDirectDebitPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): BacsDirectDebitPaymentMethod {
                return BacsDirectDebitPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    bankAccountNumber = jsonObject.getStringOrNull(BANK_ACCOUNT_NUMBER),
                    bankLocationId = jsonObject.getStringOrNull(BANK_LOCATION_ID)
                )
            }
        }
    }
}
