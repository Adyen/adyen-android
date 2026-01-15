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
    var holderNameRequired: Boolean? = null
    var isStorePaymentFieldVisible: Boolean? = null
    var shopperReference: String? = null
    var isHideCvc: Boolean? = null
    var isHideCvcStoredCard: Boolean? = null
    var socialSecurityNumberVisibility: FieldMode? = null
    var kcpAuthVisibility: FieldMode? = null

    internal fun build() = CardConfiguration(
        supportedCardBrands = supportedCardBrands,
        showHolderName = holderNameRequired,
        shopperReference = shopperReference,
        showStorePayment = isStorePaymentFieldVisible,
        hideSecurityCode = isHideCvc,
        hideStoredSecurityCode = isHideCvcStoredCard,
        socialSecurityNumberMode = socialSecurityNumberVisibility,
        koreanAuthenticationMode = kcpAuthVisibility,
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
