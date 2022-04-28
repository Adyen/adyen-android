/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/4/2021.
 */

package com.adyen.checkout.googlepay.model

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.util.AllowedAuthMethods
import com.adyen.checkout.googlepay.util.AllowedCardNetworks

private val TAG = LogUtil.getTag()

/**
 * Model class holding the parameters required to build requests for GooglePay
 */
data class GooglePayParams(
    private val googlePayConfiguration: GooglePayConfiguration,
    private val serverGatewayMerchantId: String?,
    private val availableCardNetworksFromApi: List<String>?
) {
    val gatewayMerchantId: String = getPreferredGatewayMerchantId()
    val googlePayEnvironment: Int = googlePayConfiguration.googlePayEnvironment
    val amount: Amount = googlePayConfiguration.amount
    val totalPriceStatus = googlePayConfiguration.totalPriceStatus
    val countryCode: String? = googlePayConfiguration.countryCode
    val merchantInfo: MerchantInfo? = googlePayConfiguration.merchantInfo
    val allowedAuthMethods: List<String> = getAvailableAuthMethods()
    val allowedCardNetworks: List<String> = getAvailableCardNetworks()
    val isAllowPrepaidCards: Boolean = googlePayConfiguration.isAllowPrepaidCards
    val isEmailRequired: Boolean = googlePayConfiguration.isEmailRequired
    val isExistingPaymentMethodRequired: Boolean = googlePayConfiguration.isExistingPaymentMethodRequired
    val isShippingAddressRequired: Boolean = googlePayConfiguration.isShippingAddressRequired
    val shippingAddressParameters: ShippingAddressParameters? = googlePayConfiguration.shippingAddressParameters
    val isBillingAddressRequired: Boolean = googlePayConfiguration.isBillingAddressRequired
    val billingAddressParameters: BillingAddressParameters? = googlePayConfiguration.billingAddressParameters

    /**
     * Returns the [GooglePayConfiguration.merchantAccount] if set, or falls back to the
     * paymentMethod.configuration.gatewayMerchantId field returned by the API.
     */
    private fun getPreferredGatewayMerchantId(): String {
        return googlePayConfiguration.merchantAccount
            ?: serverGatewayMerchantId
            ?: throw ComponentException(
                "GooglePay merchantAccount not found. Update your API version or pass it manually inside your " +
                    "GooglePayConfiguration"
            )
    }

    private fun getAvailableAuthMethods(): List<String> {
        return googlePayConfiguration.allowedAuthMethods
            ?: AllowedAuthMethods.allAllowedAuthMethods
    }

    private fun getAvailableCardNetworks(): List<String> {
        return googlePayConfiguration.allowedCardNetworks
            ?: getAvailableCardNetworksFromApi()
            ?: AllowedCardNetworks.allAllowedCardNetworks
    }

    private fun getAvailableCardNetworksFromApi(): List<String>? {
        if (availableCardNetworksFromApi == null) return null
        return availableCardNetworksFromApi.mapNotNull { brand ->
            val network = GooglePayParamUtils.mapBrandToGooglePayNetwork(brand)
            if (network == null) Logger.e(TAG, "skipping brand $brand, as it is not an allowed card network.")
            return@mapNotNull network
        }
    }
}
