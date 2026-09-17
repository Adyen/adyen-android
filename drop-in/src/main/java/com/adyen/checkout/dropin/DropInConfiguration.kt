/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/9/2026.
 */

package com.adyen.checkout.dropin

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize

/**
 * Configuration for Drop-in. Create it by calling [CheckoutConfiguration.dropIn].
 */
@Parcelize
class DropInConfiguration internal constructor(
    val hideStoredPaymentMethods: Boolean?,
    val startWithLastStoredPaymentMethod: Boolean?,
) : Configuration

/**
 * Adds a [DropInConfiguration] to this [CheckoutConfiguration] to configure Drop-in.
 *
 * @param hideStoredPaymentMethods Whether the stored payment methods of the shopper should be left off the payment
 * method list. Defaults to false.
 * @param startWithLastStoredPaymentMethod Whether Drop-in should open on the stored payment method the shopper used
 * last, instead of on the payment method list. Only applies if the shopper has a stored payment method, and is not
 * affected by [hideStoredPaymentMethods]. Defaults to true.
 */
fun CheckoutConfiguration.dropIn(
    hideStoredPaymentMethods: Boolean? = null,
    startWithLastStoredPaymentMethod: Boolean? = null,
): CheckoutConfiguration {
    val config = DropInConfiguration(
        hideStoredPaymentMethods = hideStoredPaymentMethods,
        startWithLastStoredPaymentMethod = startWithLastStoredPaymentMethod,
    )
    addConfiguration(config)
    return this
}
