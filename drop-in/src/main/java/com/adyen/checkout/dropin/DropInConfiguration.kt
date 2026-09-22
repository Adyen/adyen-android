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

@Parcelize
class DropInConfiguration internal constructor(
    val hideStoredPaymentMethods: Boolean?,
    val startWithLastStoredPaymentMethod: Boolean?,
) : Configuration

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
