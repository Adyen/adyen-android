/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.Configuration
import kotlinx.parcelize.Parcelize

@Parcelize
@Suppress("LongParameterList")
class GooglePayConfiguration internal constructor(
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

@Suppress("LongParameterList")
fun CheckoutConfiguration.googlePay(
    merchantAccount: String? = null,
    googlePayEnvironment: Int? = null,
    totalPriceStatus: String? = null,
    countryCode: String? = null,
    merchantInfo: MerchantInfo? = null,
    allowedAuthMethods: List<String>? = null,
    allowedCardNetworks: List<String>? = null,
    isAllowPrepaidCards: Boolean? = null,
    isAllowCreditCards: Boolean? = null,
    isAssuranceDetailsRequired: Boolean? = null,
    isEmailRequired: Boolean? = null,
    isExistingPaymentMethodRequired: Boolean? = null,
    isShippingAddressRequired: Boolean? = null,
    shippingAddressParameters: ShippingAddressParameters? = null,
    isBillingAddressRequired: Boolean? = null,
    billingAddressParameters: BillingAddressParameters? = null,
    checkoutOption: String? = null,
    googlePayButtonStyling: GooglePayButtonStyling? = null,
): CheckoutConfiguration {
    val config = GooglePayConfiguration(
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
    addConfiguration(config)
    return this
}
