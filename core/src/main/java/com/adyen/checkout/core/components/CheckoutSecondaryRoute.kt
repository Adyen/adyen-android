/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/5/2026.
 */

package com.adyen.checkout.core.components

/**
 * Represents the navigation routes that can be triggered while displaying [CheckoutSecondary].
 */
abstract class CheckoutSecondaryRoute internal constructor() {

    /**
     * Route to return to the payment method component. Use [CheckoutPaymentMethod] to display it.
     */
    class PaymentMethod : CheckoutSecondaryRoute()
}
