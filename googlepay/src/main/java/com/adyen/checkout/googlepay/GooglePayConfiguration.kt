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
    val allowedPaymentMethods: List<GooglePayPaymentMethodParameters>?,
    val isEmailRequired: Boolean?,
    val isExistingPaymentMethodRequired: Boolean?,
    val isShippingAddressRequired: Boolean?,
    val shippingAddressParameters: ShippingAddressParameters?,
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
    allowedPaymentMethods: List<GooglePayPaymentMethodParameters>? = null,
    isEmailRequired: Boolean? = null,
    isExistingPaymentMethodRequired: Boolean? = null,
    isShippingAddressRequired: Boolean? = null,
    shippingAddressParameters: ShippingAddressParameters? = null,
    checkoutOption: String? = null,
    googlePayButtonStyling: GooglePayButtonStyling? = null,
): CheckoutConfiguration {
    val config = GooglePayConfiguration(
        merchantAccount = merchantAccount,
        googlePayEnvironment = googlePayEnvironment,
        totalPriceStatus = totalPriceStatus,
        countryCode = countryCode,
        merchantInfo = merchantInfo,
        allowedPaymentMethods = allowedPaymentMethods,
        isEmailRequired = isEmailRequired,
        isExistingPaymentMethodRequired = isExistingPaymentMethodRequired,
        isShippingAddressRequired = isShippingAddressRequired,
        shippingAddressParameters = shippingAddressParameters,
        checkoutOption = checkoutOption,
        googlePayButtonStyling = googlePayButtonStyling,
    )
    addConfiguration(config)
    return this
}
