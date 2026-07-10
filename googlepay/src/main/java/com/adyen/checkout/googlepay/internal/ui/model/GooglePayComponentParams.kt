/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters

internal data class GooglePayComponentParams(
    val amount: Amount,
    val gatewayMerchantId: String,
    val googlePayEnvironment: Int,
    val totalPriceStatus: String,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedPaymentMethods: List<GooglePayPaymentMethodParams>,
    val isEmailRequired: Boolean,
    val isExistingPaymentMethodRequired: Boolean,
    val isShippingAddressRequired: Boolean,
    val shippingAddressParameters: ShippingAddressParameters?,
    val checkoutOption: String?,
    val googlePayButtonStyling: GooglePayButtonStyling?,
)
