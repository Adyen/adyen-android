/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/5/2026.
 */

package com.adyen.checkout.core.components

/**
 * Represents the navigation routes that can be triggered during the checkout flow.
 */
abstract class CheckoutRoute internal constructor() {

    /**
     * Route to display a payment method component. Use [CheckoutPaymentMethod] to display it.
     */
    class PaymentMethod : CheckoutRoute()

    /**
     * Route to display an action component. Use [CheckoutAction] to display the action.
     */
    class Action : CheckoutRoute()

    /**
     * Route to display secondary content. Use [CheckoutSecondary] to display the secondary content.
     *
     * @param identifier The unique identifier for the secondary screen.
     */
    data class Secondary(val identifier: String) : CheckoutRoute()
}
