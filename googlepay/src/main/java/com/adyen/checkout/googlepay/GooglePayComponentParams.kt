/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/11/2022.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.googlepay.model.BillingAddressParameters
import com.adyen.checkout.googlepay.model.MerchantInfo
import com.adyen.checkout.googlepay.model.ShippingAddressParameters
import java.util.Locale
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GooglePayComponentParams(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    val gatewayMerchantId: String,
    val googlePayEnvironment: Int,
    val amount: Amount,
    val totalPriceStatus: String,
    val countryCode: String?,
    val merchantInfo: MerchantInfo?,
    val allowedAuthMethods: List<String>,
    val allowedCardNetworks: List<String>,
    val isAllowPrepaidCards: Boolean,
    val isEmailRequired: Boolean,
    val isExistingPaymentMethodRequired: Boolean,
    val isShippingAddressRequired: Boolean,
    val shippingAddressParameters: ShippingAddressParameters?,
    val isBillingAddressRequired: Boolean,
    val billingAddressParameters: BillingAddressParameters?,
) : ComponentParams
