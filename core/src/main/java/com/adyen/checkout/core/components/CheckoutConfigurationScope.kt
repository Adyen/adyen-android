/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.core.components

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.components.internal.Configuration

/**
 * The receiver of the [CheckoutConfiguration] block. Use it to add drop-in or payment method specific configurations,
 * such as `dropIn(...)` or `card(...)`.
 */
@CheckoutConfigurationMarker
class CheckoutConfigurationScope internal constructor() {

    private val configurations = mutableMapOf<String, Configuration>()

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun addConfiguration(configuration: Configuration) {
        // In the future if we might need to support multiple configurations of the same type we should use the payment
        // method type as the key instead of the class name.
        configurations[configuration::class.java.name] = configuration
    }

    internal fun getConfigurations(): Map<String, Configuration> {
        return configurations.toMap()
    }
}
