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
    val amount: Amount?,
    val countryCode: String,
    val shopperLocale: String,
    val channel: String,
    val splitCardFundingSources: Boolean,
    val returnUrl: String,
    val additionalData: AdditionalData,
    val threeDSAuthenticationOnly: Boolean,
    val shopperIP: String,
    val reference: String,
    val lineItems: List<Item>,
    val threeDS2RequestData: ThreeDS2RequestDataRequest?,
    val shopperEmail: String?
)
