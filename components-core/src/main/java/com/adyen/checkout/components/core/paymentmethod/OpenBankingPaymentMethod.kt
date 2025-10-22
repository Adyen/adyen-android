/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class OpenBankingPaymentMethod(
    override var type: String? = null,
    @Deprecated("This property is deprecated. Use the SERIALIZER to send the payment data to your backend.")
    override var checkoutAttemptId: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.OPEN_BANKING

        @JvmField
        val SERIALIZER: Serializer<OpenBankingPaymentMethod> = object : Serializer<OpenBankingPaymentMethod> {
            override fun serialize(modelObject: OpenBankingPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(OpenBankingPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): OpenBankingPaymentMethod {
                return OpenBankingPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    issuer = jsonObject.getStringOrNull(ISSUER),
                )
            }
        }
    }
}
