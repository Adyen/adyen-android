/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 13/12/2019.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.example.service

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.example.data.api.model.AdditionalData
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.InstallmentPlan
import com.adyen.checkout.example.data.api.model.Item
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequestData
import com.adyen.checkout.example.data.api.model.RecurringProcessingModel
import com.adyen.checkout.example.data.api.model.RemoveStoredPaymentMethodRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.example.data.api.model.StorePaymentMethodMode
import com.adyen.checkout.example.data.api.model.ThreeDS2RequestDataRequest
import com.adyen.checkout.sessions.core.SessionSetupInstallmentOptions
import org.json.JSONObject

@Suppress("LongParameterList")
fun getPaymentMethodRequest(
    merchantAccount: String,
    shopperReference: String,
    amount: Amount?,
    countryCode: String,
    shopperLocale: String,
    splitCardFundingSources: Boolean,
    order: OrderRequest? = null
): PaymentMethodsRequest {
    return PaymentMethodsRequest(
        merchantAccount = merchantAccount,
        shopperReference = shopperReference,
        amount = if (order == null) amount else null,
        countryCode = countryCode,
        shopperLocale = shopperLocale,
        splitCardFundingSources = splitCardFundingSources,
        order = order,
        channel = CHANNEL
    )
}

@Suppress("LongParameterList")
fun getSessionRequest(
    merchantAccount: String,
    shopperReference: String,
    amount: Amount?,
    countryCode: String,
    shopperLocale: String,
    splitCardFundingSources: Boolean,
    redirectUrl: String,
    isThreeds2Enabled: Boolean,
    isExecuteThreeD: Boolean,
    installmentOptions: Map<String, SessionSetupInstallmentOptions>?,
    force3DS2Challenge: Boolean = true,
    threeDSAuthenticationOnly: Boolean = false,
    shopperEmail: String? = null,
    allowedPaymentMethods: List<String>? = null,
    storePaymentMethodMode: String? = StorePaymentMethodMode.ASK_FOR_CONSENT.mode,
    recurringProcessingModel: String? = RecurringProcessingModel.SUBSCRIPTION.recurringModel,
): SessionRequest {
    return SessionRequest(
        merchantAccount = merchantAccount,
        shopperReference = shopperReference,
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
        threeDS2RequestData = null, // if (force3DS2Challenge) ThreeDS2RequestDataRequest() else null
        shopperEmail = shopperEmail,
        allowedPaymentMethods = allowedPaymentMethods,
        storePaymentMethodMode = storePaymentMethodMode,
        recurringProcessingModel = recurringProcessingModel,
        installmentOptions = installmentOptions
    )
}

@Suppress("LongParameterList")
fun createPaymentRequest(
    paymentComponentData: JSONObject,
    shopperReference: String,
    amount: Amount,
    countryCode: String,
    merchantAccount: String,
    redirectUrl: String,
    isThreeds2Enabled: Boolean,
    isExecuteThreeD: Boolean,
    shopperEmail: String? = null,
    force3DS2Challenge: Boolean = true,
    threeDSAuthenticationOnly: Boolean = false,
    recurringProcessingModel: String? = RecurringProcessingModel.SUBSCRIPTION.recurringModel,
): PaymentsRequest {
    val paymentsRequestData = PaymentsRequestData(
        shopperReference = shopperReference,
        amount = amount,
        merchantAccount = merchantAccount,
        returnUrl = redirectUrl,
        countryCode = countryCode,
        shopperIP = SHOPPER_IP,
        reference = getReference(),
        channel = CHANNEL,
        additionalData = getAdditionalData(isThreeds2Enabled = isThreeds2Enabled, isExecuteThreeD = isExecuteThreeD),
        lineItems = LINE_ITEMS,
        shopperEmail = shopperEmail,
        threeDSAuthenticationOnly = threeDSAuthenticationOnly,
        threeDS2RequestData = if (force3DS2Challenge) ThreeDS2RequestDataRequest() else null,
        recurringProcessingModel = recurringProcessingModel,
    )

    return PaymentsRequest(paymentComponentData, paymentsRequestData)
}

fun createBalanceRequest(
    paymentComponentData: JSONObject,
    merchantAccount: String,
) = BalanceRequest(
    paymentMethod = paymentComponentData,
    merchantAccount = merchantAccount
)

fun createOrderRequest(
    amount: Amount,
    merchantAccount: String
) = CreateOrderRequest(
    amount = amount,
    merchantAccount = merchantAccount,
    reference = getReference()
)

fun createCancelOrderRequest(
    orderJson: JSONObject,
    merchantAccount: String
) = CancelOrderRequest(
    order = orderJson,
    merchantAccount = merchantAccount
)

fun createRemoveStoredPaymentMethodRequest(
    recurringDetailReference: String,
    merchantAccount: String,
    shopperReference: String
) = RemoveStoredPaymentMethodRequest(
    recurringDetailReference = recurringDetailReference,
    merchantAccount = merchantAccount,
    shopperReference = shopperReference
)

private const val SHOPPER_IP = "142.12.31.22"
private const val CHANNEL = "android"
private val LINE_ITEMS = listOf(Item())
private const val DEFAULT_INSTALLMENT_OPTION = "card"
private const val CARD_BASED_INSTALLMENT_OPTION = "visa"

private fun getReference() = "android-test-components_${System.currentTimeMillis()}"
private fun getAdditionalData(isThreeds2Enabled: Boolean, isExecuteThreeD: Boolean) = AdditionalData(
    allow3DS2 = isThreeds2Enabled.toString(),
    executeThreeD = isExecuteThreeD.toString()
)

fun getSettingsInstallmentOptionsMode(settingsInstallmentOptionMode: Int) = when (settingsInstallmentOptionMode) {
    0 -> null
    1 -> mapOf(DEFAULT_INSTALLMENT_OPTION to getSessionInstallmentOption())
    2 -> mapOf(
        DEFAULT_INSTALLMENT_OPTION to getSessionInstallmentOption(plans = listOf(InstallmentPlan.REVOLVING.plan))
    )
    else -> mapOf(CARD_BASED_INSTALLMENT_OPTION to getSessionInstallmentOption())
}

@Suppress("MagicNumber")
private fun getSessionInstallmentOption(
    plans: List<String> = listOf(InstallmentPlan.REGULAR.plan),
    preselectedValue: Int = 2,
    values: List<Int> = listOf(2, 3)
) = SessionSetupInstallmentOptions(plans, preselectedValue, values)
