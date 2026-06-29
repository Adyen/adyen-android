/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/6/2026.
 */

package com.adyen.checkout.authentication

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize

@Parcelize
internal class AuthenticationConfiguration internal constructor(
    val threeDSRequestorAppURL: String?,
) : Configuration

@JvmOverloads
fun CheckoutConfiguration.authentication(
    threeDSRequestorAppURL: String? = null,
): CheckoutConfiguration {
    val config = AuthenticationConfiguration(
        threeDSRequestorAppURL = threeDSRequestorAppURL,
    )
    addConfiguration(config)
    return this
}
