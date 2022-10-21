/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.components.model.payments.request

import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class PayByBankPaymentMethod(
    override var type: String? = null,
    override var issuer: String? = null
) : IssuerListPaymentMethod() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.PAY_BY_BANK

        @JvmField
        val SERIALIZER: Serializer<PayByBankPaymentMethod> = object : Serializer<PayByBankPaymentMethod> {
            override fun serialize(modelObject: PayByBankPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(ISSUER, modelObject.issuer)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PayByBankPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PayByBankPaymentMethod {
                return PayByBankPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    issuer = jsonObject.getStringOrNull(ISSUER)
                )
            }
        }
    }
}
