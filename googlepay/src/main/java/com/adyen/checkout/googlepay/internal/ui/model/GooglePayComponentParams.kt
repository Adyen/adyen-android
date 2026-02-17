/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.GooglePayButtonStyling
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters

internal data class GooglePayComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val amount: Amount,
    val gatewayMerchantId: String,
    val googlePayEnvironment: Int,
    val totalPriceStatus: String,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedAuthMethods: List<String>,
    val allowedCardNetworks: List<String>,
    val isAllowPrepaidCards: Boolean,
    val isAllowCreditCards: Boolean?,
    val isAssuranceDetailsRequired: Boolean?,
    val isEmailRequired: Boolean,
    val isExistingPaymentMethodRequired: Boolean,
    val isShippingAddressRequired: Boolean,
    val shippingAddressParameters: ShippingAddressParameters?,
    val isBillingAddressRequired: Boolean,
    val billingAddressParameters: BillingAddressParameters?,
    val checkoutOption: String?,
    val googlePayButtonStyling: GooglePayButtonStyling?,
) : ComponentParams by commonComponentParams
