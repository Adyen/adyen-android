/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class PayByBankUSPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
) : PaymentMethodDetails() {

    companion object {

        @JvmField
        val SERIALIZER: Serializer<PayByBankUSPaymentMethod> = object : Serializer<PayByBankUSPaymentMethod> {
            override fun serialize(modelObject: PayByBankUSPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PayByBankUSPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PayByBankUSPaymentMethod {
                return PayByBankUSPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                )
            }
        }
    }
}
