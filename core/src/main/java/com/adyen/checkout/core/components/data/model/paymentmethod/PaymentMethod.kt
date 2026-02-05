/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.json.JSONObject

/**
 * Abstract class representing a payment method from the /paymentMethods API response.
 *
 * Specific payment method types extend this class with their own fields.
 * Explicitly unsupported payment methods are deserialized as [UnsupportedPaymentMethod],
 * while other unknown types fall back to [InstantPaymentMethod].
 */
abstract class PaymentMethod
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : PaymentMethodResponse() {

    companion object {
        @Suppress("TooGenericExceptionThrown")
        @JvmField
        val SERIALIZER: Serializer<PaymentMethod> = object : Serializer<PaymentMethod> {
            override fun serialize(modelObject: PaymentMethod): JSONObject {
                val paymentMethodType = with(modelObject.type) {
                    if (isNullOrEmpty()) {
                        // TODO - Error Propagation
                        // throw CheckoutException("PaymentMethod type not found")
                        throw RuntimeException("PaymentMethod type not found")
                    } else {
                        this
                    }
                }
                val serializer = getChildSerializer(paymentMethodType)
                return serializer.serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethod {
                val type = jsonObject.getStringOrNull(TYPE)
                if (type.isNullOrEmpty()) {
                    // TODO - Error Propagation
                    // throw CheckoutException("PaymentMethod type not found")
                    throw RuntimeException("PaymentMethod type not found")
                }
                val serializer = getChildSerializer(type)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("CyclomaticComplexMethod")
        fun getChildSerializer(paymentMethodType: String): Serializer<PaymentMethod> {
            val serializer = when (paymentMethodType) {
                PaymentMethodTypes.SCHEME -> CardPaymentMethod.SERIALIZER
                in PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS -> UnsupportedPaymentMethod.SERIALIZER
                else -> InstantPaymentMethod.SERIALIZER
            }

            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<PaymentMethod>
        }
    }
}
