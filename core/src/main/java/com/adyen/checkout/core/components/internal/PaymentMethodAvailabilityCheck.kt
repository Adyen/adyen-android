/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod

/**
 * Optional check to verify whether a payment method is available in the current environment.
 *
 * A [PaymentComponentFactory] can implement this interface to check requirements such as device
 * capabilities or installed apps before a component is created. Factories that do not implement
 * this interface are considered always available.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentMethodAvailabilityCheck {

    /**
     * Returns whether the payment method can be used in the current environment.
     */
    suspend fun isAvailable(
        paymentMethod: PaymentMethod,
        params: CheckoutParams,
    ): Boolean
}
