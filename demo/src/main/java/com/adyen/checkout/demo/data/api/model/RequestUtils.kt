/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.data.api.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.sessions.core.SessionSetupInstallmentOptions

@Suppress("LongParameterList")
fun getSessionRequest(
    amount: Amount?,
    countryCode: String,
    shopperLocale: String,
    splitCardFundingSources: Boolean,
    redirectUrl: String,
    isThreeds2Enabled: Boolean,
    isExecuteThreeD: Boolean,
    installmentOptions: Map<String, SessionSetupInstallmentOptions>?,
    showInstallmentAmount: Boolean = false,
    threeDSAuthenticationOnly: Boolean = false,
    shopperEmail: String? = null,
    allowedPaymentMethods: List<String>? = null,
    storePaymentMethodMode: String? = "askForConsent",
    recurringProcessingModel: String? = "Subscription",
): SessionRequest {
    return SessionRequest(
        amount = amount,
        countryCode = countryCode,
        shopperLocale = shopperLocale,
        splitCardFundingSources = splitCardFundingSources,
        returnUrl = redirectUrl,
        shopperIP = SHOPPER_IP,
        reference = getReference(),
        channel = CHANNEL,
        additionalData = getAdditionalData(isThreeds2Enabled = isThreeds2Enabled, isExecuteThreeD = isExecuteThreeD),
        lineItems = LINE_ITEMS,
        threeDSAuthenticationOnly = threeDSAuthenticationOnly,
        // TODO check if this should be kept or removed
        //  previous code: if (force3DS2Challenge) ThreeDS2RequestDataRequest() else null
        threeDS2RequestData = null,
        shopperEmail = shopperEmail,
        allowedPaymentMethods = allowedPaymentMethods,
        storePaymentMethodMode = storePaymentMethodMode,
        recurringProcessingModel = recurringProcessingModel,
        installmentOptions = installmentOptions,
        showInstallmentAmount = showInstallmentAmount,
    )
}

private fun getReference() = "android-test-components_${System.currentTimeMillis()}"

private fun getAdditionalData(isThreeds2Enabled: Boolean, isExecuteThreeD: Boolean) = AdditionalData(
    allow3DS2 = isThreeds2Enabled.toString(),
    executeThreeD = isExecuteThreeD.toString(),
)

private const val SHOPPER_IP = "142.12.31.22"
private const val CHANNEL = "android"
private val LINE_ITEMS = listOf(Item())
