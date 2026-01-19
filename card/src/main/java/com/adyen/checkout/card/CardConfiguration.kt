/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize

@Parcelize
@Suppress("LongParameterList")
class CardConfiguration(
    val showHolderName: Boolean?,
    val supportedCardBrands: List<CardBrand>?,
    val shopperReference: String?,
    val showStorePayment: Boolean?,
    val hideSecurityCode: Boolean?,
    val hideStoredSecurityCode: Boolean?,
    val socialSecurityNumberMode: FieldMode?,
    val koreanAuthenticationMode: FieldMode?,
    // TODO - Card. Installments & Address
) : Configuration

class CardConfigurationBuilder internal constructor() {

    var supportedCardBrands: List<CardBrand>? = null
    var showHolderName: Boolean? = null
    var showStorePayment: Boolean? = null
    var shopperReference: String? = null
    var hideSecurityCode: Boolean? = null
    var hideStoredSecurityCode: Boolean? = null
    var socialSecurityNumberMode: FieldMode? = null
    var koreanAuthenticationMode: FieldMode? = null

    internal fun build() = CardConfiguration(
        supportedCardBrands = supportedCardBrands,
        showHolderName = showHolderName,
        shopperReference = shopperReference,
        showStorePayment = showStorePayment,
        hideSecurityCode = hideSecurityCode,
        hideStoredSecurityCode = hideStoredSecurityCode,
        socialSecurityNumberMode = socialSecurityNumberMode,
        koreanAuthenticationMode = koreanAuthenticationMode,
    )
}

fun CheckoutConfiguration.card(
    configuration: @CheckoutConfigurationMarker CardConfigurationBuilder.() -> Unit = {},
): CheckoutConfiguration {
    val config = CardConfigurationBuilder()
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.SCHEME, config)
    return this
}

internal fun CheckoutConfiguration.getCardConfiguration(): CardConfiguration? {
    return getConfiguration(PaymentMethodTypes.SCHEME)
}
