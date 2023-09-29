/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.internal.util.AllowedAuthMethods
import com.adyen.checkout.googlepay.internal.util.AllowedCardNetworks
import com.google.android.gms.wallet.WalletConstants

internal class GooglePayComponentParamsMapper(
    private val overrideComponentParams: ComponentParams?,
    private val overrideSessionParams: SessionParams?,
) {

    fun mapToParams(
        googlePayConfiguration: GooglePayConfiguration,
        paymentMethod: PaymentMethod,
        sessionParams: SessionParams?,
    ): GooglePayComponentParams {
        return googlePayConfiguration
            .mapToParamsInternal(paymentMethod)
            .override(overrideComponentParams)
            .override(sessionParams ?: overrideSessionParams)
    }

    private fun GooglePayConfiguration.mapToParamsInternal(
        paymentMethod: PaymentMethod,
    ): GooglePayComponentParams {
        return GooglePayComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = AnalyticsParams(analyticsConfiguration),
            isCreatedByDropIn = false,
            gatewayMerchantId = getPreferredGatewayMerchantId(paymentMethod),
            allowedAuthMethods = getAvailableAuthMethods(),
            allowedCardNetworks = getAvailableCardNetworks(paymentMethod),
            googlePayEnvironment = getGooglePayEnvironment(),
            amount = amount ?: DEFAULT_AMOUNT,
            totalPriceStatus = totalPriceStatus ?: DEFAULT_TOTAL_PRICE_STATUS,
            countryCode = countryCode,
            merchantInfo = merchantInfo,
            isAllowPrepaidCards = isAllowPrepaidCards ?: false,
            isAllowCreditCards = isAllowCreditCards,
            isAssuranceDetailsRequired = isAssuranceDetailsRequired,
            isEmailRequired = isEmailRequired ?: false,
            isExistingPaymentMethodRequired = isExistingPaymentMethodRequired ?: false,
            isShippingAddressRequired = isShippingAddressRequired ?: false,
            shippingAddressParameters = shippingAddressParameters,
            isBillingAddressRequired = isBillingAddressRequired ?: false,
            billingAddressParameters = billingAddressParameters,
        )
    }

    /**
     * Returns the [GooglePayConfiguration.merchantAccount] if set, or falls back to the
     * paymentMethod.configuration.gatewayMerchantId field returned by the API.
     */
    private fun GooglePayConfiguration.getPreferredGatewayMerchantId(
        paymentMethod: PaymentMethod,
    ): String {
        return merchantAccount
            ?: paymentMethod.configuration?.gatewayMerchantId
            ?: throw ComponentException(
                "GooglePay merchantAccount not found. Update your API version or pass it manually inside your " +
                    "GooglePayConfiguration"
            )
    }

    private fun GooglePayConfiguration.getAvailableAuthMethods(): List<String> {
        return allowedAuthMethods
            ?: AllowedAuthMethods.allAllowedAuthMethods
    }

    private fun GooglePayConfiguration.getAvailableCardNetworks(
        paymentMethod: PaymentMethod
    ): List<String> {
        return allowedCardNetworks
            ?: getAvailableCardNetworksFromApi(paymentMethod)
            ?: AllowedCardNetworks.allAllowedCardNetworks
    }

    private fun getAvailableCardNetworksFromApi(paymentMethod: PaymentMethod): List<String>? {
        val brands = paymentMethod.brands ?: return null
        return brands.mapNotNull { brand ->
            val network = mapBrandToGooglePayNetwork(brand)
            if (network == null) Logger.e(TAG, "skipping brand $brand, as it is not an allowed card network.")
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

    private fun GooglePayConfiguration.getGooglePayEnvironment(): Int {
        return when {
            googlePayEnvironment != null -> googlePayEnvironment
            environment == Environment.TEST -> WalletConstants.ENVIRONMENT_TEST
            else -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }

    private fun GooglePayComponentParams.override(overrideComponentParams: ComponentParams?): GooglePayComponentParams {
        if (overrideComponentParams == null) return this
        val amount = overrideComponentParams.amount ?: DEFAULT_AMOUNT
        return copy(
            shopperLocale = overrideComponentParams.shopperLocale,
            environment = overrideComponentParams.environment,
            clientKey = overrideComponentParams.clientKey,
            analyticsParams = overrideComponentParams.analyticsParams,
            isCreatedByDropIn = overrideComponentParams.isCreatedByDropIn,
            amount = amount,
        )
    }

    private fun GooglePayComponentParams.override(
        sessionParams: SessionParams? = null
    ): GooglePayComponentParams {
        if (sessionParams == null) return this
        return copy(
            amount = sessionParams.amount ?: amount,
        )
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private val DEFAULT_AMOUNT = Amount(currency = CheckoutCurrency.USD.name, value = 0)
        private const val DEFAULT_TOTAL_PRICE_STATUS = "FINAL"
    }
}
