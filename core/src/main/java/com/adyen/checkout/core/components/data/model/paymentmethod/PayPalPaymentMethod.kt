/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2026.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.exception.ModelSerializationException
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * A [PaymentMethod] representing a PayPal payment method.
 */
@Parcelize
data class PayPalPaymentMethod(
    override val type: String,
    override val name: String,
) : PaymentMethod() {

    companion object {
        @JvmField
        val SERIALIZER: Serializer<PayPalPaymentMethod> = object : Serializer<PayPalPaymentMethod> {
            override fun serialize(modelObject: PayPalPaymentMethod): JSONObject {
                return try {
                    JSONObject().apply {
                        put(TYPE, modelObject.type)
                        put(NAME, modelObject.name)
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PayPalPaymentMethod::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PayPalPaymentMethod {
                return try {
                    PayPalPaymentMethod(
                        type = jsonObject.getString(TYPE),
                        name = jsonObject.getString(NAME),
                    )
                } catch (e: JSONException) {
                    throw ModelSerializationException(PayPalPaymentMethod::class.java, e)
                }
            }
        }
    }
}
