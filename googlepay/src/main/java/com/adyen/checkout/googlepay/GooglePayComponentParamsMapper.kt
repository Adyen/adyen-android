/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.googlepay.model.GooglePayParamUtils
import com.adyen.checkout.googlepay.util.AllowedAuthMethods
import com.adyen.checkout.googlepay.util.AllowedCardNetworks
import com.google.android.gms.wallet.WalletConstants

internal class GooglePayComponentParamsMapper(
    private val parentConfiguration: Configuration?
) {

    fun mapToParams(
        googlePayConfiguration: GooglePayConfiguration,
        paymentMethod: PaymentMethod,
    ): GooglePayComponentParams {
        return mapToParams(
            parentConfiguration = parentConfiguration ?: googlePayConfiguration,
            googlePayConfiguration = googlePayConfiguration,
            paymentMethod = paymentMethod,
        )
    }

    private fun mapToParams(
        parentConfiguration: Configuration,
        googlePayConfiguration: GooglePayConfiguration,
        paymentMethod: PaymentMethod,
    ): GooglePayComponentParams {
        with(googlePayConfiguration) {
            return GooglePayComponentParams(
                shopperLocale = parentConfiguration.shopperLocale,
                environment = parentConfiguration.environment,
                clientKey = parentConfiguration.clientKey,
                gatewayMerchantId = getPreferredGatewayMerchantId(googlePayConfiguration, paymentMethod),
                allowedAuthMethods = getAvailableAuthMethods(googlePayConfiguration),
                allowedCardNetworks = getAvailableCardNetworks(googlePayConfiguration, paymentMethod),
                googlePayEnvironment = getGooglePayEnvironment(googlePayConfiguration),
                amount = amount,
                totalPriceStatus = totalPriceStatus,
                countryCode = countryCode,
                merchantInfo = merchantInfo,
                isAllowPrepaidCards = isAllowPrepaidCards,
                isEmailRequired = isEmailRequired,
                isExistingPaymentMethodRequired = isExistingPaymentMethodRequired,
                isShippingAddressRequired = isShippingAddressRequired,
                shippingAddressParameters = shippingAddressParameters,
                isBillingAddressRequired = isBillingAddressRequired,
                billingAddressParameters = billingAddressParameters,
            )
        }
    }

    /**
     * Returns the [GooglePayConfiguration.merchantAccount] if set, or falls back to the
     * paymentMethod.configuration.gatewayMerchantId field returned by the API.
     */
    private fun getPreferredGatewayMerchantId(
        googlePayConfiguration: GooglePayConfiguration,
        paymentMethod: PaymentMethod,
    ): String {
        return googlePayConfiguration.merchantAccount
            ?: paymentMethod.configuration?.gatewayMerchantId
            ?: throw ComponentException(
                "GooglePay merchantAccount not found. Update your API version or pass it manually inside your " +
                    "GooglePayConfiguration"
            )
    }

    private fun getAvailableAuthMethods(googlePayConfiguration: GooglePayConfiguration): List<String> {
        return googlePayConfiguration.allowedAuthMethods
            ?: AllowedAuthMethods.allAllowedAuthMethods
    }

    private fun getAvailableCardNetworks(
        googlePayConfiguration: GooglePayConfiguration,
        paymentMethod: PaymentMethod
    ): List<String> {
        return googlePayConfiguration.allowedCardNetworks
            ?: getAvailableCardNetworksFromApi(paymentMethod)
            ?: AllowedCardNetworks.allAllowedCardNetworks
    }

    private fun getAvailableCardNetworksFromApi(paymentMethod: PaymentMethod): List<String>? {
        val brands = paymentMethod.brands ?: return null
        return brands.mapNotNull { brand ->
            val network = GooglePayParamUtils.mapBrandToGooglePayNetwork(brand)
            if (network == null) Logger.e(TAG, "skipping brand $brand, as it is not an allowed card network.")
            return@mapNotNull network
        }
    }

    private fun getGooglePayEnvironment(googlePayConfiguration: GooglePayConfiguration): Int {
        val googlePayEnvironment = googlePayConfiguration.googlePayEnvironment
        return when {
            googlePayEnvironment != null -> googlePayEnvironment
            googlePayConfiguration.environment == Environment.TEST -> WalletConstants.ENVIRONMENT_TEST
            else -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
