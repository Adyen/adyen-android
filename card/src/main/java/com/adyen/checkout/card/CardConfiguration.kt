/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/10/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import kotlinx.parcelize.Parcelize

@Parcelize
@Suppress("LongParameterList")
class CardConfiguration internal constructor(
    val billingAddressMode: BillingAddressMode?,
    val koreanAuthenticationVisibility: FieldVisibility?,
    val showCardholderName: Boolean?,
    val showSecurityCode: Boolean?,
    val showSecurityCodeForStoredCard: Boolean?,
    val showStorePaymentMethod: Boolean?,
    val showSupportedCardBrandLogos: Boolean?,
    val socialSecurityNumberVisibility: FieldVisibility?,
    val supportedCardBrands: List<CardBrand>?,
    // TODO - Card. Installments
) : Configuration

@Suppress("LongParameterList")
fun CheckoutConfiguration.card(
    billingAddressMode: BillingAddressMode? = null,
    koreanAuthenticationVisibility: FieldVisibility? = null,
    showCardholderName: Boolean? = null,
    showSecurityCode: Boolean? = null,
    showSecurityCodeForStoredCard: Boolean? = null,
    showStorePaymentMethod: Boolean? = null,
    showSupportedCardBrandLogos: Boolean? = null,
    socialSecurityNumberVisibility: FieldVisibility? = null,
    supportedCardBrands: List<CardBrand>? = null,
): CheckoutConfiguration {
    val config = CardConfiguration(
        billingAddressMode = billingAddressMode,
        koreanAuthenticationVisibility = koreanAuthenticationVisibility,
        showCardholderName = showCardholderName,
        showSecurityCode = showSecurityCode,
        showSecurityCodeForStoredCard = showSecurityCodeForStoredCard,
        showStorePaymentMethod = showStorePaymentMethod,
        showSupportedCardBrandLogos = showSupportedCardBrandLogos,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        supportedCardBrands = supportedCardBrands,
    )
    addConfiguration(PaymentMethodTypes.SCHEME, config)
    return this
}

internal fun CheckoutConfiguration.getCardConfiguration(): CardConfiguration? {
    return getConfiguration(PaymentMethodTypes.SCHEME)
}
