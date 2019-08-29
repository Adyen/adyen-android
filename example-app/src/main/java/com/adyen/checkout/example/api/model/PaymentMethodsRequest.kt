/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.example.api.model

import com.adyen.checkout.base.model.payments.Amount

@Suppress("MagicNumber")
data class PaymentMethodsRequest(
    val merchantAccount: String,
    val shopperReference: String,
//    val additionalData: Any,
//    val allowedPaymentMethods: ArrayList<String>,
    val amount: Amount,
//    val blockedPaymentMethods: ArrayList<String>,
    val countryCode: String = "NL",
    val shopperLocale: String = "en_US",
    val channel: String = "android"
)
