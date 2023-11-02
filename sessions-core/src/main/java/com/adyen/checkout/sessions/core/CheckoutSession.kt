/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodCustomDisplayInformation
import com.adyen.checkout.components.core.StoredPaymentMethod

/**
 * A class holding the data required to launch Drop-in or a component with the sessions flow.
 * Use [CheckoutSessionProvider.createSession] to create this class.
 */
data class CheckoutSession(
    val sessionSetupResponse: SessionSetupResponse,
    val order: Order?,
) {

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
    @Suppress("unused")
    fun addPaymentMethodCustomDisplayInformation(
        type: String,
        customDisplayInformation: PaymentMethodCustomDisplayInformation,
        predicate: (PaymentMethod) -> Boolean = { true }
    ) = sessionSetupResponse.paymentMethodsApiResponse?.addPaymentMethodCustomDisplayInformation(
        type,
        customDisplayInformation,
        predicate
    )

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
    @Suppress("unused")
    fun addStoredPaymentMethodCustomDisplayInformation(
        type: String,
        customDisplayInformation: PaymentMethodCustomDisplayInformation,
        predicate: (StoredPaymentMethod) -> Boolean = { true }
    ) = sessionSetupResponse.paymentMethodsApiResponse?.addStoredPaymentMethodCustomDisplayInformation(
        type,
        customDisplayInformation,
        predicate
    )

    fun getPaymentMethod(paymentMethodType: String): PaymentMethod? {
        return sessionSetupResponse.paymentMethodsApiResponse?.paymentMethods.orEmpty().firstOrNull {
            it.type == paymentMethodType
        }
    }
}
