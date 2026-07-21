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

/**
 * Configuration for the Google Pay payment method. Create it by calling [CheckoutConfiguration.googlePay].
 */
@Parcelize
@Suppress("LongParameterList")
class GooglePayConfiguration internal constructor(
    val merchantAccount: String?,
    val googlePayEnvironment: Int?,
    val totalPriceStatus: String?,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedPaymentMethods: GooglePayAllowedPaymentMethods?,
    val isEmailRequired: Boolean?,
    val isExistingPaymentMethodRequired: Boolean?,
    val isShippingAddressRequired: Boolean?,
    val shippingAddressParameters: ShippingAddressParameters?,
    val checkoutOption: String?,
    val googlePayButtonStyling: GooglePayButtonStyling?,
) : Configuration

/**
 * Adds a [GooglePayConfiguration] to this [CheckoutConfiguration] to configure the Google Pay payment method.
 *
 * Most parameters map to fields of the Google Pay request objects. See the
 * [Google Pay docs](https://developers.google.com/pay/api/android/reference/request-objects) for more details.
 *
 * @param merchantAccount The merchant account to be put in the payment token from Google to Adyen. If not set, the
 * value from the `/paymentMethods` response is used.
 * @param googlePayEnvironment The Google Pay environment, either `WalletConstants.ENVIRONMENT_TEST` or
 * `WalletConstants.ENVIRONMENT_PRODUCTION`. Defaults to the value of the Adyen environment.
 * @param totalPriceStatus The status of the total price used. Defaults to `"FINAL"`.
 * @param countryCode The ISO 3166-1 alpha-2 country code where the transaction is processed.
 * @param merchantInfo Information about the merchant requesting the payment.
 * @param allowedPaymentMethods The payment methods offered through Google Pay and their parameters. See
 * [GooglePayAllowedPaymentMethods]. Defaults to a single card payment method when null.
 * @param isEmailRequired Whether an email address is required.
 * @param isExistingPaymentMethodRequired Whether an existing payment method is required for the shopper to be
 * considered ready to pay.
 * @param isShippingAddressRequired Whether a shipping address is required.
 * @param shippingAddressParameters The required shipping address details.
 * @param checkoutOption The checkout option, which affects the submit button text shown in the Google Pay sheet.
 * @param googlePayButtonStyling The customization of the Google Pay button.
 */
@Suppress("LongParameterList")
fun CheckoutConfiguration.googlePay(
    merchantAccount: String? = null,
    googlePayEnvironment: Int? = null,
    totalPriceStatus: String? = null,
    countryCode: String? = null,
    merchantInfo: MerchantInfo? = null,
    allowedPaymentMethods: GooglePayAllowedPaymentMethods? = null,
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
