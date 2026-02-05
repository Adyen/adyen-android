/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutCurrency
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.google.android.gms.wallet.WalletConstants

internal class GooglePayComponentParamsMapper {

    fun mapToParams(
        componentParamsBundle: ComponentParamsBundle,
        googlePayConfiguration: GooglePayConfiguration,
        paymentMethod: PaymentMethod,
    ): GooglePayComponentParams {
        // TODO - Pass isCreatedByDropIn
        val commonComponentParams = componentParamsBundle.commonComponentParams
        return GooglePayComponentParams(
            commonComponentParams = commonComponentParams,
            amount = commonComponentParams.amount ?: DEFAULT_AMOUNT,
            gatewayMerchantId = googlePayConfiguration.getPreferredGatewayMerchantId(paymentMethod),
            allowedAuthMethods = googlePayConfiguration.getAvailableAuthMethods(),
            allowedCardNetworks = googlePayConfiguration.getAvailableCardNetworks(paymentMethod),
            googlePayEnvironment = getGooglePayEnvironment(
                commonComponentParams.environment,
                googlePayConfiguration
            ),
            totalPriceStatus = googlePayConfiguration.totalPriceStatus
                ?: DEFAULT_TOTAL_PRICE_STATUS,
            countryCode = googlePayConfiguration.countryCode,
            merchantInfo = googlePayConfiguration.merchantInfo,
            isAllowPrepaidCards = googlePayConfiguration.isAllowPrepaidCards ?: false,
            isAllowCreditCards = googlePayConfiguration.isAllowCreditCards,
            isAssuranceDetailsRequired = googlePayConfiguration.isAssuranceDetailsRequired,
            isEmailRequired = googlePayConfiguration.isEmailRequired ?: false,
            isExistingPaymentMethodRequired = googlePayConfiguration.isExistingPaymentMethodRequired
                ?: false,
            isShippingAddressRequired = googlePayConfiguration.isShippingAddressRequired ?: false,
            shippingAddressParameters = googlePayConfiguration.shippingAddressParameters,
            isBillingAddressRequired = googlePayConfiguration.isBillingAddressRequired ?: false,
            billingAddressParameters = googlePayConfiguration.billingAddressParameters,
            checkoutOption = googlePayConfiguration.checkoutOption,
            googlePayButtonStyling = googlePayConfiguration.googlePayButtonStyling,
        )
    }

    /**
     * Returns the [GooglePayConfiguration.merchantAccount] if set, or falls back to the
     * paymentMethod.configuration.gatewayMerchantId field returned by the API.
     */
    @Suppress("TooGenericExceptionThrown")
    private fun GooglePayConfiguration?.getPreferredGatewayMerchantId(
        paymentMethod: PaymentMethod,
    ): String {
        // TODO - Change RuntimeException into a clearer error. Also remove the suppresion.
        return this?.merchantAccount
            ?: paymentMethod.configuration?.gatewayMerchantId
            ?: throw RuntimeException(
                "GooglePay merchantAccount not found. Update your API version or pass it manually inside your " +
                    "GooglePayConfiguration",
            )
    }

    private fun GooglePayConfiguration?.getAvailableAuthMethods(): List<String> {
        return this?.allowedAuthMethods
            ?: AllowedAuthMethods.allAllowedAuthMethods
    }

    private fun GooglePayConfiguration?.getAvailableCardNetworks(
        paymentMethod: PaymentMethod
    ): List<String> {
        return this?.allowedCardNetworks
            ?: getAvailableCardNetworksFromApi(paymentMethod)
            ?: AllowedCardNetworks.allAllowedCardNetworks
    }

    private fun getAvailableCardNetworksFromApi(paymentMethod: PaymentMethod): List<String>? {
        val brands = paymentMethod.brands ?: return null
        return brands.mapNotNull { brand ->
            val network = mapBrandToGooglePayNetwork(brand)
            if (network == null) {
                adyenLog(AdyenLogLevel.ERROR) { "skipping brand $brand, as it is not an allowed card network." }
            }
            return@mapNotNull network
        }
    }

    private fun mapBrandToGooglePayNetwork(brand: String): String? {
        return when {
            brand == "mc" -> AllowedCardNetworks.MASTERCARD
            AllowedCardNetworks.allAllowedCardNetworks.contains(brand.uppercase()) -> brand.uppercase()
            else -> null
        }
    }

    private fun getGooglePayEnvironment(
        environment: Environment,
        googlePayConfiguration: GooglePayConfiguration,
    ): Int {
        return when {
            googlePayConfiguration.googlePayEnvironment != null -> googlePayConfiguration.googlePayEnvironment
            environment == Environment.TEST -> WalletConstants.ENVIRONMENT_TEST
            else -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }

    companion object {
        private val DEFAULT_AMOUNT = Amount(currency = CheckoutCurrency.USD.name, value = 0)
        private const val DEFAULT_TOTAL_PRICE_STATUS = "FINAL"
    }
}
