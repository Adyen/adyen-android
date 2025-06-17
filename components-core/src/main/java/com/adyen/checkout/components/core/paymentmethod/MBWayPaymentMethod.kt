/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.components.core.paymentmethod

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class MBWayPaymentMethod(
    override var type: String?,
    override var checkoutAttemptId: String?,
    var telephoneNumber: String?,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.MB_WAY
        private const val TELEPHONE_NUMBER = "telephoneNumber"

        @JvmField
        val SERIALIZER: Serializer<MBWayPaymentMethod> = object : Serializer<MBWayPaymentMethod> {
            override fun serialize(modelObject: MBWayPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(TELEPHONE_NUMBER, modelObject.telephoneNumber)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(GooglePayPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): MBWayPaymentMethod {
                return MBWayPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    telephoneNumber = jsonObject.getStringOrNull(TELEPHONE_NUMBER),
                )
            }
        }
    }
}
