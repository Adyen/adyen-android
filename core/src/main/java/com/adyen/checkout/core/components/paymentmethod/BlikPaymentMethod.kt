/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class BlikPaymentMethod(
    override val type: String?,
    override val checkoutAttemptId: String?,
    val blikCode: String?,
    val storedPaymentMethodId: String?,
) : PaymentMethodDetails() {

    companion object {
        const val PAYMENT_METHOD_TYPE = PaymentMethodTypes.BLIK
        private const val BLIK_CODE = "blikCode"
        private const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<BlikPaymentMethod> = object : Serializer<BlikPaymentMethod> {
            override fun serialize(modelObject: BlikPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(TYPE, modelObject.type)
                        putOpt(CHECKOUT_ATTEMPT_ID, modelObject.checkoutAttemptId)
                        putOpt(BLIK_CODE, modelObject.blikCode)
                        putOpt(STORED_PAYMENT_METHOD_ID, modelObject.storedPaymentMethodId)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(BlikPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): BlikPaymentMethod {
                return BlikPaymentMethod(
                    type = jsonObject.getStringOrNull(TYPE),
                    checkoutAttemptId = jsonObject.getStringOrNull(CHECKOUT_ATTEMPT_ID),
                    blikCode = jsonObject.getStringOrNull(BLIK_CODE),
                    storedPaymentMethodId = jsonObject.getStringOrNull(STORED_PAYMENT_METHOD_ID),
                )
            }
        }
    }
}
