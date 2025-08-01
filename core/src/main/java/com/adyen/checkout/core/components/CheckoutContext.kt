/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/7/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.sessions.CheckoutSession

// TODO - Kdocs
sealed interface CheckoutContext {
    data class Sessions internal constructor(
        val checkoutSession: CheckoutSession,
        val checkoutConfiguration: CheckoutConfiguration,
        val checkoutCallbacks: CheckoutCallbacks?
    ) : CheckoutContext

    // TODO - Investigate different use cases
    sealed interface Advanced: CheckoutContext {

        data class Payment internal constructor(
            val paymentMethodsApiResponse: PaymentMethodsApiResponse,
            val checkoutConfiguration: CheckoutConfiguration,
            val checkoutCallbacks: CheckoutCallbacks,
        ): Advanced

        data class Action internal constructor(
            val checkoutConfiguration: CheckoutConfiguration,
            val actionCallbacks: ActionCallbacks,
        ): Advanced

    }
}
