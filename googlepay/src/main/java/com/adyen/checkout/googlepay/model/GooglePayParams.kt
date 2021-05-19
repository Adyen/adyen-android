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
import com.adyen.checkout.googlepay.GooglePayConfiguration

/**
 * Model class holding the parameters required to build requests for GooglePay
 */
data class GooglePayParams(
    private val googlePayConfiguration: GooglePayConfiguration,
    private val serverGatewayMerchantId: String?
) {
    val gatewayMerchantId: String = getPreferredGatewayMerchantId()
    val googlePayEnvironment: Int = googlePayConfiguration.googlePayEnvironment
    val amount: Amount = googlePayConfiguration.amount
    val countryCode: String? = googlePayConfiguration.countryCode
    val merchantInfo: MerchantInfo? = googlePayConfiguration.merchantInfo
    val allowedAuthMethods: List<String>? = googlePayConfiguration.allowedAuthMethods
    val allowedCardNetworks: List<String>? = googlePayConfiguration.allowedCardNetworks
    val isAllowPrepaidCards: Boolean = googlePayConfiguration.isAllowPrepaidCards
    val isEmailRequired: Boolean = googlePayConfiguration.isEmailRequired
    val isExistingPaymentMethodRequired: Boolean = googlePayConfiguration.isExistingPaymentMethodRequired
    val isShippingAddressRequired: Boolean = googlePayConfiguration.isShippingAddressRequired
    val shippingAddressParameters: ShippingAddressParameters? = googlePayConfiguration.shippingAddressParameters
    val isBillingAddressRequired: Boolean = googlePayConfiguration.isBillingAddressRequired
    val billingAddressParameters: BillingAddressParameters? = googlePayConfiguration.billingAddressParameters

    /**
     * Returns the gatewayMerchantId provided by the API if available, or falls back to the one provided in GooglePayConfiguration
     */
    private fun getPreferredGatewayMerchantId(): String {
        return serverGatewayMerchantId
            ?: googlePayConfiguration.merchantAccount
            ?: throw ComponentException(
                "GooglePay merchantAccount not found. Update your API version or pass it manually inside your GooglePayConfiguration"
            )
    }
}
