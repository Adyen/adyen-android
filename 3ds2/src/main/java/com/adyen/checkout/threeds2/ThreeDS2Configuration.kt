/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.threeds2

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ThreeDS2Configuration internal constructor(
    val threeDSRequestorAppURL: String?,
) : Configuration

@JvmOverloads
fun CheckoutConfiguration.threeDS2(
    threeDSRequestorAppURL: String? = null,
): CheckoutConfiguration {
    val config = ThreeDS2Configuration(
        threeDSRequestorAppURL = threeDSRequestorAppURL,
    )
    addConfiguration(config)
    return this
}
