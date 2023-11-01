/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */
package com.adyen.checkout.components.core

import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.ModelObject
import com.adyen.checkout.core.internal.data.model.ModelUtils.deserializeOptList
import com.adyen.checkout.core.internal.data.model.ModelUtils.serializeOptList
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Object that parses and holds the response data from the /paymentMethods endpoint.
 * Use [PaymentMethodsApiResponse.SERIALIZER] to deserialize this class from your JSON response.
 */
@Parcelize
data class PaymentMethodsApiResponse(
    var storedPaymentMethods: List<StoredPaymentMethod>? = null,
    var paymentMethods: List<PaymentMethod>? = null,
) : ModelObject() {

    // TODO: Create a filter {} lambda for custom display name
    fun addCustomDisplayInformation(type: String, customDisplayInformation: PaymentMethodCustomDisplayInformation) =
        paymentMethods?.filter { it.type == type }?.forEach {
            it.customDisplayInformation = customDisplayInformation
        }

    companion object {
        private const val STORED_PAYMENT_METHODS = "storedPaymentMethods"
        private const val PAYMENT_METHODS = "paymentMethods"

        @JvmField
        val SERIALIZER: Serializer<PaymentMethodsApiResponse> = object : Serializer<PaymentMethodsApiResponse> {
            override fun serialize(modelObject: PaymentMethodsApiResponse): JSONObject {
                return try {
                    JSONObject().apply {
                        putOpt(
                            STORED_PAYMENT_METHODS,
                            serializeOptList(modelObject.storedPaymentMethods, StoredPaymentMethod.SERIALIZER)
                        )
                        putOpt(PAYMENT_METHODS, serializeOptList(modelObject.paymentMethods, PaymentMethod.SERIALIZER))
                    }
                } catch (e: JSONException) {
                    throw ModelSerializationException(PaymentMethodsApiResponse::class.java, e)
                }
            }

            override fun deserialize(jsonObject: JSONObject): PaymentMethodsApiResponse {
                return PaymentMethodsApiResponse(
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
