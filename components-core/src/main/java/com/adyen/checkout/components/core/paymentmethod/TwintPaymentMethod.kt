/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class TwintPaymentMethod(
    override var type: String?,
    var subtype: String?,
    override var checkoutAttemptId: String?,
) : PaymentMethodDetails() {

    companion object {

        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.TWINT

        private const val SUBTYPE = "subtype"

        @JvmField
        val SERIALIZER: Serializer<TwintPaymentMethod> = object : Serializer<TwintPaymentMethod> {
            override fun serialize(modelObject: TwintPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(SUBTYPE, modelObject.subtype)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(TwintPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): TwintPaymentMethod {
                return TwintPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    subtype = jsonObject.getStringOrNull(SUBTYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                )
            }
        }
    }
}
