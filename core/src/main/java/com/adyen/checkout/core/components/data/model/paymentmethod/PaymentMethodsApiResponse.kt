/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/1/2026.
 */

package com.adyen.checkout.core.components.data.model.paymentmethod

import com.adyen.checkout.core.common.internal.model.ModelObject
import com.adyen.checkout.core.common.internal.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.common.internal.model.ModelUtils.serializeOptList
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Object that parses and holds the response data from the /paymentMethods endpoint.
 * Use [PaymentMethodsApiResponse.SERIALIZER] to deserialize this class from your JSON response.
 */
@Parcelize
data class PaymentMethodsApiResponse(
    val storedPaymentMethods: List<StoredPaymentMethod>? = null,
    val paymentMethods: List<PaymentMethod>? = null,
) : ModelObject() {

    companion object {
        private const val STORED_PAYMENT_METHODS = "storedPaymentMethods"
        private const val PAYMENT_METHODS = "paymentMethods"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethodsApiResponse> = object : Serializer<PaymentMethodsApiResponse> {
            override fun serialize(modelObject: PaymentMethodsApiResponse): JSONObject {
                return JSONObject().apply {
                    putOpt(
                        STORED_PAYMENT_METHODS,
                        serializeOptList(modelObject.storedPaymentMethods, StoredPaymentMethod.SERIALIZER),
                    )
                    putOpt(
                        PAYMENT_METHODS,
                        serializeOptList(
                            modelObject.paymentMethods,
                            PaymentMethod.SERIALIZER,
                        ),
                    )
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethodsApiResponse {
                return PaymentMethodsApiResponse(
                    storedPaymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(STORED_PAYMENT_METHODS),
                        StoredPaymentMethod.SERIALIZER,
                    ),
                    paymentMethods = deserializeOptList(
                        jsonObject.optJSONArray(PAYMENT_METHODS),
                        PaymentMethod.SERIALIZER,
                    ),
                )
            }
        }
    }
}
