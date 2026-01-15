/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import kotlinx.parcelize.Parcelize

@Parcelize
class BlikConfiguration : Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BlikConfigurationBuilder internal constructor() {

    internal fun build() = BlikConfiguration()
}

fun CheckoutConfiguration.blik(
    configuration: @CheckoutConfigurationMarker BlikConfigurationBuilder.() -> Unit = {},
): CheckoutConfiguration {
    val config = BlikConfigurationBuilder()
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.BLIK, config)
    return this
}

internal fun CheckoutConfiguration.getBlikConfiguration(): BlikConfiguration? {
    return getConfiguration(PaymentMethodTypes.BLIK)
}
