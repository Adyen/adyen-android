/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class SepaPaymentMethod(
    override var type: String?,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String?,
    var ownerName: String?,
    var iban: String?,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.SEPA
        private const val OWNER_NAME = "ownerName"
        private const val IBAN = "iban"

        @JvmField
        val SERIALIZER: Serializer<SepaPaymentMethod> = object : Serializer<SepaPaymentMethod> {
            override fun serialize(modelObject: SepaPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(OWNER_NAME, modelObject.ownerName)
                        putOpt(IBAN, modelObject.iban)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(SepaPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): SepaPaymentMethod {
                return SepaPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    ownerName = jsonObject.getStringOrNull(OWNER_NAME),
                    iban = jsonObject.getStringOrNull(IBAN),
                )
            }
        }
    }
}
