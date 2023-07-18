/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.googlepay.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.googlepay.BillingAddressParameters
import com.adyen.checkout.googlepay.MerchantInfo
import com.adyen.checkout.googlepay.ShippingAddressParameters
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class GooglePayComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean,
    override val isCreatedByDropIn: Boolean,
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
) : ComponentParams
