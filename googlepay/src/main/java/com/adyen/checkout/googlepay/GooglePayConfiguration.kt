/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.core.common.internal.helper.CheckoutConfigurationMarker
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.checkout.googlepay.internal.ui.GooglePayComponent
import kotlinx.parcelize.Parcelize

/**
 * Configuration class for the GooglePayComponent.
 */
@Parcelize
@Suppress("LongParameterList")
class GooglePayConfiguration(
    val merchantAccount: String?,
    val googlePayEnvironment: Int?,
    val totalPriceStatus: String?,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedAuthMethods: List<String>?,
    val allowedCardNetworks: List<String>?,
    val isAllowPrepaidCards: Boolean?,
    val isAllowCreditCards: Boolean?,
    val isAssuranceDetailsRequired: Boolean?,
    val isEmailRequired: Boolean?,
    val isExistingPaymentMethodRequired: Boolean?,
    val isShippingAddressRequired: Boolean?,
    val shippingAddressParameters: ShippingAddressParameters?,
    val isBillingAddressRequired: Boolean?,
    val billingAddressParameters: BillingAddressParameters?,
    val checkoutOption: String?,
    val googlePayButtonStyling: GooglePayButtonStyling?,
) : Configuration

class GooglePayConfigurationBuilder internal constructor() {

    var merchantAccount: String? = null
    var googlePayEnvironment: Int? = null
    var totalPriceStatus: String? = null
    var countryCode: String? = null
    var merchantInfo: MerchantInfo? = null
    var allowedAuthMethods: List<String>? = null
    var allowedCardNetworks: List<String>? = null
    var isAllowPrepaidCards: Boolean? = null
    var isAllowCreditCards: Boolean? = null
    var isAssuranceDetailsRequired: Boolean? = null
    var isEmailRequired: Boolean? = null
    var isExistingPaymentMethodRequired: Boolean? = null
    var isShippingAddressRequired: Boolean? = null
    var shippingAddressParameters: ShippingAddressParameters? = null
    var isBillingAddressRequired: Boolean? = null
    var billingAddressParameters: BillingAddressParameters? = null
    var checkoutOption: String? = null
    var googlePayButtonStyling: GooglePayButtonStyling? = null

    internal fun build() = GooglePayConfiguration(
        merchantAccount = merchantAccount,
        googlePayEnvironment = googlePayEnvironment,
        totalPriceStatus = totalPriceStatus,
        countryCode = countryCode,
        merchantInfo = merchantInfo,
        allowedAuthMethods = allowedAuthMethods,
        allowedCardNetworks = allowedCardNetworks,
        isAllowPrepaidCards = isAllowPrepaidCards,
        isAllowCreditCards = isAllowCreditCards,
        isAssuranceDetailsRequired = isAssuranceDetailsRequired,
        isEmailRequired = isEmailRequired,
        isExistingPaymentMethodRequired = isExistingPaymentMethodRequired,
        isShippingAddressRequired = isShippingAddressRequired,
        shippingAddressParameters = shippingAddressParameters,
        isBillingAddressRequired = isBillingAddressRequired,
        billingAddressParameters = billingAddressParameters,
        checkoutOption = checkoutOption,
        googlePayButtonStyling = googlePayButtonStyling,
    )
}

fun CheckoutConfiguration.googlePay(
    configuration: @CheckoutConfigurationMarker GooglePayConfigurationBuilder.() -> Unit = {},
): CheckoutConfiguration {
    val config = GooglePayConfigurationBuilder()
        .apply(configuration)
        .build()
    GooglePayComponent.PAYMENT_METHOD_TYPES.forEach { key ->
        addConfiguration(key, config)
    }
    return this
}

internal fun CheckoutConfiguration.getGooglePayConfiguration(): GooglePayConfiguration? {
    return GooglePayComponent.PAYMENT_METHOD_TYPES.firstNotNullOfOrNull { key ->
        getConfiguration<GooglePayConfiguration>(key)
    }
}
