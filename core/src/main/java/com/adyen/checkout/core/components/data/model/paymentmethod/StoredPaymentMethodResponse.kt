/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/1/2025.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.json.JSONObject

/**
 * Abstract class representing a stored payment method from the /paymentMethods API response.
 *
 * Specific stored payment method types extend this class with their own fields.
 * Unknown unsupported stored payment methods are deserialized as [StoredUnsupportedPaymentMethodResponse].
 */
abstract class StoredPaymentMethodResponse
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : PaymentMethodResponse() {
    abstract val id: String
    abstract val supportedShopperInteractions: List<String>

    companion object {
        const val ID = "id"
        const val SUPPORTED_SHOPPER_INTERACTIONS = "supportedShopperInteractions"

        @Suppress("TooGenericExceptionThrown")
        @JvmField
        val SERIALIZER: Serializer<StoredPaymentMethodResponse> = object : Serializer<StoredPaymentMethodResponse> {
            override fun serialize(modelObject: StoredPaymentMethodResponse): JSONObject {
                val serializer = getChildSerializer(modelObject.type)
                return serializer.serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): StoredPaymentMethodResponse {
                val type = jsonObject.getString(TYPE)
                val serializer = getChildSerializer(type)
                return serializer.deserialize(jsonObject)
            }
        }

        @Suppress("CyclomaticComplexMethod")
        private fun getChildSerializer(paymentMethodType: String): Serializer<StoredPaymentMethodResponse> {
            val serializer = when (paymentMethodType) {
                PaymentMethodTypes.SCHEME -> StoredCardPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.BCMC -> StoredBCMCPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.BLIK -> StoredBLIKPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.ACH -> StoredACHDirectDebitPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.CASH_APP_PAY -> StoredCashAppPayPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.TWINT -> StoredTwintPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.PAY_BY_BANK_US -> StoredPayByBankUSPaymentMethodResponse.SERIALIZER
                PaymentMethodTypes.PAY_TO -> StoredPayToPaymentMethodResponse.SERIALIZER
                else -> StoredUnsupportedPaymentMethodResponse.SERIALIZER
            }

            @Suppress("UNCHECKED_CAST")
            return serializer as Serializer<StoredPaymentMethodResponse>
        }
    }
}
