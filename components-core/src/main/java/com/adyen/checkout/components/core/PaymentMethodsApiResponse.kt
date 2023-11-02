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

    /**
     * Allows setting custom display information for payment methods, allowing filter by [type] and [predicate].
     *
     * Calling this function multiple times will cause the custom display information to be overridden.
     * This might be useful when [customDisplayInformation] should be updated when localization settings change.
     *
     * @param type Updates payment methods matching the given type.
     * @param customDisplayInformation Customizable information object to override the default display values.
     * @param predicate Updates payment methods not only matching the type but also matching the given predicate.
     */
    fun addPaymentMethodCustomDisplayInformation(
        type: String,
        customDisplayInformation: PaymentMethodCustomDisplayInformation,
        predicate: (PaymentMethod) -> Boolean = { true }
    ) = paymentMethods
        ?.filter { paymentMethod -> paymentMethod.type == type && predicate(paymentMethod) }
        ?.forEach { paymentMethod -> paymentMethod.customDisplayInformation = customDisplayInformation }

    /**
     * Allows setting custom display information for stored payment methods, allowing filter by [type] and [predicate].
     *
     * Calling this function multiple times will cause the custom display information to be overridden.
     * This might be useful when [customDisplayInformation] should be updated when localization settings change.
     *
     * @param type Updates stored payment methods matching the given type.
     * @param customDisplayInformation Customizable information object to override the default display values.
     * @param predicate Updates stored payment methods not only matching the type but also matching the given predicate.
     */
    fun addStoredPaymentMethodCustomDisplayInformation(
        type: String,
        customDisplayInformation: PaymentMethodCustomDisplayInformation,
        predicate: (StoredPaymentMethod) -> Boolean = { true }
    ) = storedPaymentMethods
        ?.filter { storedPaymentMethod -> storedPaymentMethod.type == type && predicate(storedPaymentMethod) }
        ?.forEach { storedPaymentMethod -> storedPaymentMethod.customDisplayInformation = customDisplayInformation }

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
