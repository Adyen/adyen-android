/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.api.model.paymentsRequest

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.OrderRequest

data class PaymentMethodsRequest(
    val merchantAccount: String,
    val shopperReference: String,
//    val additionalData: Any,
//    val allowedPaymentMethods: ArrayList<String>,
    val amount: Amount?,
//    val blockedPaymentMethods: ArrayList<String>,
    val countryCode: String = "NL",
    val shopperLocale: String = "en_US",
    val channel: String = "android",
    val splitCardFundingSources: Boolean = false,
    val order: OrderRequest?
)
