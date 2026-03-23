/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.old.internal.data.model.ModelUtils.serializeOptList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Object that parses and holds the response data from the /paymentMethods endpoint.
 * Use [PaymentMethods.SERIALIZER] to deserialize this class from your JSON response.
 */
@Parcelize
data class PaymentMethods(
    var storedPaymentMethods: List<StoredPaymentMethod>? = null,
    var paymentMethods: List<PaymentMethod>? = null,
) : ModelObject() {

    companion object {
        private const val STORED_PAYMENT_METHODS = "storedPaymentMethods"
        private const val PAYMENT_METHODS = "paymentMethods"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethods> = object : Serializer<PaymentMethods> {
            override fun serialize(modelObject: PaymentMethods): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(
                            STORED_PAYMENT_METHODS,
                            serializeOptList(modelObject.storedPaymentMethods, StoredPaymentMethod.SERIALIZER)
                        )
                        putOpt(PAYMENT_METHODS, serializeOptList(modelObject.paymentMethods, PaymentMethod.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethods::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethods {
                return PaymentMethods(
                    storedPaymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(STORED_PAYMENT_METHODS),
                        StoredPaymentMethod.SERIALIZER
                    ),
                    paymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(PAYMENT_METHODS),
                        PaymentMethod.SERIALIZER
                    ),
                )
            }
        }
    }
}
