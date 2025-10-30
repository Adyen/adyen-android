/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.parcelize.Parcelize

@Parcelize
internal class Adyen3DS2Configuration(
    val uiCustomization: UiCustomization?,
    val threeDSRequestorAppURL: String?,
) : Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class Adyen3DS2ConfigurationBuilder internal constructor() {

    var uiCustomization: UiCustomization? = null
    var threeDSRequestorAppURL: String? = null

    internal fun build() = Adyen3DS2Configuration(
        uiCustomization = uiCustomization,
        threeDSRequestorAppURL = threeDSRequestorAppURL,
    )
}

fun CheckoutConfiguration.adyen3ds2(
    configuration: @CheckoutConfigurationMarker Adyen3DS2ConfigurationBuilder.() -> Unit = {}
): CheckoutConfiguration {
    val config = Adyen3DS2ConfigurationBuilder()
        .apply(configuration)
        .build()
    addActionConfiguration(config)
    return this
}

internal fun CheckoutConfiguration.getAdyen3DS2Configuration(): Adyen3DS2Configuration? {
    return getActionConfiguration(Adyen3DS2Configuration::class.java)
}
