/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/3/2022.
 */

package com.adyen.checkout.example.data.api.model

import com.adyen.checkout.components.model.payments.Amount

data class SessionRequest(
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
    val returnUrl: String
)
