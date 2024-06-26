/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.ButtonParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters

internal data class GooglePayComponentParams(
    private val commonComponentParams: CommonComponentParams,
    override val amount: Amount,
    override val isSubmitButtonVisible: Boolean,
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
) : ComponentParams by commonComponentParams, ButtonParams
