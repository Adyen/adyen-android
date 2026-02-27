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
 * Abstract class representing a stored payment method from the /paymentMethods API response.
 *
 * Specific stored payment method types extend this class with their own fields.
 * Explicitly unsupported stored payment methods are deserialized as [StoredUnsupportedPaymentMethod],
 * while other unknown types fall back to [StoredInstantPaymentMethod].
 */
abstract class StoredPaymentMethod
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : PaymentMethodResponse() {
    abstract val id: String
    abstract val supportedShopperInteractions: List<String>

    // TODO - Should this be moved to the usage sight?
    val isEcommerce: Boolean
        get() = supportedShopperInteractions.contains(ECOMMERCE)

    companion object {
        const val ID = "id"
        const val SUPPORTED_SHOPPER_INTERACTIONS = "supportedShopperInteractions"
        private const val ECOMMERCE = "Ecommerce"

        @Suppress("TooGenericExceptionThrown")
        @JvmField
        val SERIALIZER: Serializer<StoredPaymentMethod> = object : Serializer<StoredPaymentMethod> {
            override fun serialize(modelObject: StoredPaymentMethod): JSONObject {
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

            override fun deserialize(jsonObject: JSONObject): StoredPaymentMethod {
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
        fun getChildSerializer(paymentMethodType: String): Serializer<StoredPaymentMethod> {
            val serializer = when (paymentMethodType) {
                PaymentMethodTypes.SCHEME -> StoredCardPaymentMethod.SERIALIZER
                PaymentMethodTypes.BCMC -> StoredBCMCPaymentMethod.SERIALIZER
                PaymentMethodTypes.BLIK -> StoredBLIKPaymentMethod.SERIALIZER
                PaymentMethodTypes.ACH -> StoredACHDirectDebitPaymentMethod.SERIALIZER
                PaymentMethodTypes.CASH_APP_PAY -> StoredCashAppPayPaymentMethod.SERIALIZER
                PaymentMethodTypes.TWINT -> StoredTwintPaymentMethod.SERIALIZER
                PaymentMethodTypes.PAY_BY_BANK_US -> StoredPayByBankUSPaymentMethod.SERIALIZER
                PaymentMethodTypes.PAY_TO -> StoredPayToPaymentMethod.SERIALIZER
                in PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS -> StoredUnsupportedPaymentMethod.SERIALIZER
                else -> StoredInstantPaymentMethod.SERIALIZER
            }

            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<StoredPaymentMethod>
        }
    }
}
