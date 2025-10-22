/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class BacsDirectDebitPaymentMethod(
    override var type: String?,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String?,
    var holderName: String?,
    var bankAccountNumber: String?,
    var bankLocationId: String?,
) : PaymentMethodDetails() {

    companion object {
        private const val HOLDER_NAME = "holderName"
        private const val BANK_ACCOUNT_NUMBER = "bankAccountNumber"
        private const val BANK_LOCATION_ID = "bankLocationId"

        const val PAYMENT_METHOD_TYPE = "directdebit_GB"

        @JvmField
        val SERIALIZER: Serializer<BacsDirectDebitPaymentMethod> = object : Serializer<BacsDirectDebitPaymentMethod> {

            override fun serialize(modelObject: BacsDirectDebitPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
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
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    holderName = jsonObject.getStringOrNull(HOLDER_NAME),
                    bankAccountNumber = jsonObject.getStringOrNull(BANK_ACCOUNT_NUMBER),
                    bankLocationId = jsonObject.getStringOrNull(BANK_LOCATION_ID)
                )
            }
        }
    }
}
