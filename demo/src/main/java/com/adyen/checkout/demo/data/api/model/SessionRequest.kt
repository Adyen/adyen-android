/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.data.api.model

import androidx.annotation.Keep
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.sessions.core.SessionSetupInstallmentOptions

@Keep
data class SessionRequest(
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
    val shopperEmail: String?,
    val allowedPaymentMethods: List<String>?,
    val storePaymentMethodMode: String?,
    val recurringProcessingModel: String?,
    val installmentOptions: Map<String, SessionSetupInstallmentOptions>?,
    val showInstallmentAmount: Boolean
)

