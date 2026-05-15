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
    val showCardholderName: Boolean?,
    val supportedCardBrands: List<CardBrand>?,
    val showStorePaymentMethod: Boolean?,
    val showSecurityCode: Boolean?,
    val showSecurityCodeForStoredCard: Boolean?,
    val showSupportedCardBrandLogos: Boolean?,
    val socialSecurityNumberVisibility: FieldVisibility?,
    val koreanAuthenticationVisibility: FieldVisibility?,
    val billingAddressMode: BillingAddressMode?,
    // TODO - Card. Installments
) : Configuration

class CardConfigurationBuilder internal constructor() {

    var showCardholderName: Boolean? = null
    var supportedCardBrands: List<CardBrand>? = null
    var showStorePaymentMethod: Boolean? = null
    var showSecurityCode: Boolean? = null
    var showSecurityCodeForStoredCard: Boolean? = null
    var showSupportedCardBrandLogos: Boolean? = null
    var socialSecurityNumberVisibility: FieldVisibility? = null
    var koreanAuthenticationVisibility: FieldVisibility? = null
    var billingAddressMode: BillingAddressMode? = null

    internal fun build() = CardConfiguration(
        showCardholderName = showCardholderName,
        supportedCardBrands = supportedCardBrands,
        showStorePaymentMethod = showStorePaymentMethod,
        showSecurityCode = showSecurityCode,
        showSecurityCodeForStoredCard = showSecurityCodeForStoredCard,
        showSupportedCardBrandLogos = showSupportedCardBrandLogos,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        koreanAuthenticationVisibility = koreanAuthenticationVisibility,
        billingAddressMode = billingAddressMode,
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
