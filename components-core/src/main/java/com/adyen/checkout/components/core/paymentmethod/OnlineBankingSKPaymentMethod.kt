/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class OnlineBankingSKPaymentMethod(
    override var type: String? = null,
    override var checkoutAttemptId: String? = null,
    override var issuer: String? = null,
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.ONLINE_BANKING_SK

        @JvmField
        val SERIALIZER: Serializer<OnlineBankingSKPaymentMethod> = object : Serializer<OnlineBankingSKPaymentMethod> {
            override fun serialize(modelObject: OnlineBankingSKPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(OnlineBankingSKPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): OnlineBankingSKPaymentMethod {
                return OnlineBankingSKPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
