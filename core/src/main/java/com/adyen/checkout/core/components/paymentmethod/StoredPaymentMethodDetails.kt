/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/8/2026.
 */

package com.adyen.checkout.core.components.paymentmethod

import androidx.annotation.RestrictTo
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class StoredPaymentMethodDetails : PaymentMethodDetails() {

    abstract val storedPaymentMethodId: String?

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    companion object {
        const val STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId"

        @JvmField
        val SERIALIZER: Serializer<StoredPaymentMethodDetails> = object : Serializer<StoredPaymentMethodDetails> {

            override fun serialize(modelObject: StoredPaymentMethodDetails): JSONObject {
                val serializer = getChildSerializer(modelObject.type)
                return serializer.serialize(modelObject)
            }

            override fun deserialize(jsonObject: JSONObject): StoredPaymentMethodDetails {
                val paymentMethodType = jsonObject.getString(TYPE)
                val serializer = getChildSerializer(paymentMethodType)
                return serializer.deserialize(jsonObject)
            }

            fun getChildSerializer(paymentMethodType: String): Serializer<StoredPaymentMethodDetails> {
                val serializer = when (paymentMethodType) {
                    StoredCardDetails.PAYMENT_METHOD_TYPE -> StoredCardDetails.SERIALIZER
                    else -> GenericStoredDetails.SERIALIZER
                }
                @Suppress("UNCHECKED_CAST")
                return serializer as Serializer<StoredPaymentMethodDetails>
            }
        }
    }
}
