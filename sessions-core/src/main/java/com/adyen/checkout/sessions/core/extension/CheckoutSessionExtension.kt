/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/11/2023.
 */

package com.adyen.checkout.sessions.core.extension

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodCustomDisplayInformation
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.extension.addPaymentMethodCustomDisplayInformation
import com.adyen.checkout.components.core.extension.addStoredPaymentMethodCustomDisplayInformation
import com.adyen.checkout.sessions.core.CheckoutSession

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
fun CheckoutSession.addPaymentMethodCustomDisplayInformation(
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
fun CheckoutSession.addStoredPaymentMethodCustomDisplayInformation(
    type: String,
    customDisplayInformation: PaymentMethodCustomDisplayInformation,
    predicate: (StoredPaymentMethod) -> Boolean = { true }
) = sessionSetupResponse.paymentMethodsApiResponse?.addStoredPaymentMethodCustomDisplayInformation(
    type,
    customDisplayInformation,
    predicate
)
