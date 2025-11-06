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
    val isHolderNameRequired: Boolean?,
    val supportedCardBrands: List<CardBrand>?,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean?,
    val isHideCvc: Boolean?,
    val isHideCvcStoredCard: Boolean?,
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility?,
    val kcpAuthVisibility: KCPAuthVisibility?,
    // TODO - Card. Installments & Address
) : Configuration

class CardConfigurationBuilder internal constructor() {

    var supportedCardBrands: List<CardBrand>? = null
    var holderNameRequired: Boolean? = null
    var isStorePaymentFieldVisible: Boolean? = null
    var shopperReference: String? = null
    var isHideCvc: Boolean? = null
    var isHideCvcStoredCard: Boolean? = null
    var socialSecurityNumberVisibility: SocialSecurityNumberVisibility? = null
    var kcpAuthVisibility: KCPAuthVisibility? = null

    internal fun build() = CardConfiguration(
        supportedCardBrands = supportedCardBrands,
        isHolderNameRequired = holderNameRequired,
        shopperReference = shopperReference,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
        isHideCvc = isHideCvc,
        isHideCvcStoredCard = isHideCvcStoredCard,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        kcpAuthVisibility = kcpAuthVisibility,
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
