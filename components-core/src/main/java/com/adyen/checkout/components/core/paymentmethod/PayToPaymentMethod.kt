/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/1/2025.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class PayToPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
    var shopperAccountIdentifier: String? = null,
    var storedPaymentMethodId: String? = null,
) : PaymentMethodDetails() {

    companion object {

        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.PAY_TO
        private const val SHOPPER_ACCOUNT_IDENTIFIER = "shopperAccountIdentifier"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<PayToPaymentMethod> = object : Serializer<PayToPaymentMethod> {
            override fun serialize(modelObject: PayToPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(SHOPPER_ACCOUNT_IDENTIFIER, modelObject.shopperAccountIdentifier)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PayToPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PayToPaymentMethod {
                return PayToPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    shopperAccountIdentifier = jsonObject.getStringOrNull(SHOPPER_ACCOUNT_IDENTIFIER),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                )
            }
        }
    }
}
