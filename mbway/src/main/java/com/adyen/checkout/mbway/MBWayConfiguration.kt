/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.checkout.mbway.internal.ui.MBWayComponent
import kotlinx.parcelize.Parcelize

// TODO - Implement MBWayConfigurationBuilder
/**
 * Configuration class for the [MBWayComponent].
 */
@Parcelize
@Suppress("LongParameterList")
internal class MBWayConfiguration : Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class MBWayConfigurationBuilder internal constructor() {

    internal fun build() = MBWayConfiguration()
}

fun CheckoutConfiguration.mbWay(
    configuration: @CheckoutConfigurationMarker MBWayConfigurationBuilder.() -> Unit = {},
): CheckoutConfiguration {
    val config = MBWayConfigurationBuilder()
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.MB_WAY, config)
    return this
}

internal fun CheckoutConfiguration.getMBWayConfiguration(): MBWayConfiguration? {
    return getConfiguration(PaymentMethodTypes.MB_WAY)
}
