/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutCurrency
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.GooglePayPaymentMethodParameters
import com.google.android.gms.wallet.WalletConstants

internal class GooglePayComponentParamsMapper {

    fun mapToParams(
        params: CheckoutParams,
        paymentMethod: GooglePayPaymentMethod,
    ): GooglePayComponentParams {
        val googlePayConfiguration = params.getConfiguration<GooglePayConfiguration>()
        // TODO - Pass isCreatedByDropIn
        return GooglePayComponentParams(
            amount = params.amount ?: DEFAULT_AMOUNT,
            gatewayMerchantId = googlePayConfiguration.getPreferredGatewayMerchantId(paymentMethod),
            allowedPaymentMethods = googlePayConfiguration.getAllowedPaymentMethods(paymentMethod),
            googlePayEnvironment = getGooglePayEnvironment(
                params.environment,
                googlePayConfiguration,
            ),
            totalPriceStatus = googlePayConfiguration?.totalPriceStatus
                ?: DEFAULT_TOTAL_PRICE_STATUS,
            countryCode = googlePayConfiguration?.countryCode,
            merchantInfo = googlePayConfiguration?.merchantInfo,
            isEmailRequired = googlePayConfiguration?.isEmailRequired ?: false,
            isExistingPaymentMethodRequired = googlePayConfiguration?.isExistingPaymentMethodRequired
                ?: false,
            isShippingAddressRequired = googlePayConfiguration?.isShippingAddressRequired ?: false,
            shippingAddressParameters = googlePayConfiguration?.shippingAddressParameters,
            checkoutOption = googlePayConfiguration?.checkoutOption,
            googlePayButtonStyling = googlePayConfiguration?.googlePayButtonStyling,
        )
    }

    /**
     * Returns the [GooglePayConfiguration.merchantAccount] if set, or falls back to the
     * paymentMethod.configuration.gatewayMerchantId field returned by the API.
     */
    private fun GooglePayConfiguration?.getPreferredGatewayMerchantId(
        paymentMethod: GooglePayPaymentMethod,
    ): String {
        // TODO - Error propagation - Update the code
        return this?.merchantAccount
            ?: paymentMethod.configuration?.gatewayMerchantId
            ?: throw GenericError(
                message = "GooglePay merchantAccount not found. Update your API version or pass it " +
                    "manually inside your GooglePayConfiguration",
            )
    }

    /**
     * Resolves the configured [GooglePayPaymentMethodParameters]. When none are configured a single
     * [GooglePayPaymentMethodParameters.Card] is used by default. Type specific defaults (such as
     * auth methods and card networks) are filled in during resolution.
     */
    private fun GooglePayConfiguration?.getAllowedPaymentMethods(
        paymentMethod: GooglePayPaymentMethod,
    ): List<GooglePayPaymentMethodParams> {
        val configuredPaymentMethods = this?.allowedPaymentMethods?.takeIf { it.isNotEmpty() }
            ?: listOf(GooglePayPaymentMethodParameters.Card())
        return configuredPaymentMethods.map { it.resolveDefaults(paymentMethod) }
    }

    private fun GooglePayPaymentMethodParameters.resolveDefaults(
        paymentMethod: GooglePayPaymentMethod,
    ): GooglePayPaymentMethodParams {
        return when (this) {
            is GooglePayPaymentMethodParameters.Card -> GooglePayPaymentMethodParams.Card(
                allowedAuthMethods = allowedAuthMethods ?: AllowedAuthMethods.allAllowedAuthMethods,
                allowedCardNetworks = allowedCardNetworks ?: getAvailableCardNetworksFromApi(paymentMethod)
                    .ifEmpty { AllowedCardNetworks.allAllowedCardNetworks },
                isAllowPrepaidCards = allowPrepaidCards ?: false,
                isAllowCreditCards = allowCreditCards,
                isAssuranceDetailsRequired = assuranceDetailsRequired,
                isBillingAddressRequired = billingAddressRequired ?: false,
                billingAddressParameters = billingAddressParameters,
            )

            else -> error("Unsupported Google Pay payment method parameters: $this")
        }
    }

    private fun getAvailableCardNetworksFromApi(paymentMethod: GooglePayPaymentMethod): List<String> {
        val brands = paymentMethod.brands
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
        googlePayConfiguration: GooglePayConfiguration?,
    ): Int {
        return when {
            googlePayConfiguration?.googlePayEnvironment != null -> googlePayConfiguration.googlePayEnvironment
            environment == Environment.TEST -> WalletConstants.ENVIRONMENT_TEST
            else -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }

    companion object {
        private val DEFAULT_AMOUNT = Amount(currency = CheckoutCurrency.USD.name, value = 0)
        private const val DEFAULT_TOTAL_PRICE_STATUS = "FINAL"
    }
}
