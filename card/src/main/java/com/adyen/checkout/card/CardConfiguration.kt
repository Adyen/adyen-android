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
import kotlinx.parcelize.Parcelize

/**
 * Configuration for the card payment method. Create it by calling [CheckoutConfiguration.card].
 */
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
    val showCardScanner: Boolean?,
    val installmentConfiguration: InstallmentConfiguration?,
) : Configuration

/**
 * Adds a [CardConfiguration] to this [CheckoutConfiguration] to configure the card payment method.
 *
 * @param billingAddressMode The type of billing address form to show to the shopper.
 * @param koreanAuthenticationVisibility Whether the security fields for Korean cards should be shown.
 * @param showCardholderName Whether the cardholder name should be shown as an input field.
 * @param showSecurityCode Whether the security code (CVC/CVV) field should be shown and requested from the shopper on
 * a regular payment. Note that hiding it might have implications for the risk of the transaction; talk to Adyen
 * Support before changing this.
 * @param showSecurityCodeForStoredCard Whether the security code (CVC/CVV) field should be shown and requested from
 * the shopper on a stored card payment.
 * @param showStorePaymentMethod Whether the option to store the card for future payments should be shown as an input
 * field. Not applicable for the sessions flow.
 * @param showSupportedCardBrandLogos Whether the logos of the supported card brands should be shown.
 * @param socialSecurityNumberVisibility Whether the CPF/CNPJ field for Brazilian shoppers should be shown.
 * @param supportedCardBrands The supported card brands, shown as the shopper inputs the card number. Defaults to the
 * brands from the `/paymentMethods` response if available.
 * @param showCardScanner Whether the card scanner should be shown.
 * @param installmentConfiguration The configuration of the installment options shown to the shopper. Not applicable
 * for the sessions flow.
 */
@Suppress("LongParameterList")
@JvmOverloads
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
    showCardScanner: Boolean? = null,
    installmentConfiguration: InstallmentConfiguration? = null,
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
        showCardScanner = showCardScanner,
        installmentConfiguration = installmentConfiguration,
    )
    addConfiguration(config)
    return this
}
