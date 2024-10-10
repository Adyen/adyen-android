/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/11/2022.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.googlepay.AllowedAuthMethods
import com.adyen.checkout.googlepay.AllowedCardNetworks
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.getGooglePayConfiguration
import com.google.android.gms.wallet.WalletConstants
import java.util.Locale

internal class GooglePayComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        paymentMethod: PaymentMethod,
    ): GooglePayComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        val googlePayConfiguration = checkoutConfiguration.getGooglePayConfiguration()
        return mapToParams(
            commonComponentParamsMapperData.commonComponentParams,
            googlePayConfiguration,
            paymentMethod,
        )
    }

    private fun mapToParams(
        commonComponentParams: CommonComponentParams,
        googlePayConfiguration: GooglePayConfiguration?,
        paymentMethod: PaymentMethod,
    ): GooglePayComponentParams {
        return GooglePayComponentParams(
            commonComponentParams = commonComponentParams,
            amount = commonComponentParams.amount ?: DEFAULT_AMOUNT,
            isSubmitButtonVisible = shouldShowSubmitButton(commonComponentParams, googlePayConfiguration),
            gatewayMerchantId = googlePayConfiguration.getPreferredGatewayMerchantId(paymentMethod),
            allowedAuthMethods = googlePayConfiguration.getAvailableAuthMethods(),
            allowedCardNetworks = googlePayConfiguration.getAvailableCardNetworks(paymentMethod),
            googlePayEnvironment = getGooglePayEnvironment(commonComponentParams.environment, googlePayConfiguration),
            totalPriceStatus = googlePayConfiguration?.totalPriceStatus ?: DEFAULT_TOTAL_PRICE_STATUS,
            countryCode = googlePayConfiguration?.countryCode,
            merchantInfo = googlePayConfiguration?.merchantInfo,
            isAllowPrepaidCards = googlePayConfiguration?.isAllowPrepaidCards ?: false,
            isAllowCreditCards = googlePayConfiguration?.isAllowCreditCards,
            isAssuranceDetailsRequired = googlePayConfiguration?.isAssuranceDetailsRequired,
            isEmailRequired = googlePayConfiguration?.isEmailRequired ?: false,
            isExistingPaymentMethodRequired = googlePayConfiguration?.isExistingPaymentMethodRequired ?: false,
            isShippingAddressRequired = googlePayConfiguration?.isShippingAddressRequired ?: false,
            shippingAddressParameters = googlePayConfiguration?.shippingAddressParameters,
            isBillingAddressRequired = googlePayConfiguration?.isBillingAddressRequired ?: false,
            billingAddressParameters = googlePayConfiguration?.billingAddressParameters,
        )
    }

    private fun shouldShowSubmitButton(
        commonComponentParams: CommonComponentParams,
        googlePayConfiguration: GooglePayConfiguration?,
    ): Boolean {
        return if (commonComponentParams.isCreatedByDropIn) {
            false
        } else {
            // TODO return true by default in v6
            googlePayConfiguration?.isSubmitButtonVisible ?: false
        }
    }

    /**
     * Returns the [GooglePayConfiguration.merchantAccount] if set, or falls back to the
     * paymentMethod.configuration.gatewayMerchantId field returned by the API.
     */
    private fun GooglePayConfiguration?.getPreferredGatewayMerchantId(
        paymentMethod: PaymentMethod,
    ): String {
        return this?.merchantAccount
            ?: paymentMethod.configuration?.gatewayMerchantId
            ?: throw ComponentException(
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
        googlePayConfiguration: GooglePayConfiguration?
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
