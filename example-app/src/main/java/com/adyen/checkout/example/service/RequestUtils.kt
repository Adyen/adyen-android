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
import com.adyen.checkout.example.data.api.model.AuthenticationData
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.InstallmentPlan
import com.adyen.checkout.example.data.api.model.Item
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequestData
import com.adyen.checkout.example.data.api.model.RecurringProcessingModel
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.example.data.api.model.StorePaymentMethodMode
import com.adyen.checkout.example.data.api.model.ThreeDSRequestData
import com.adyen.checkout.example.data.storage.CardInstallmentOptionsMode
import com.adyen.checkout.example.data.storage.ThreeDSMode
import com.adyen.checkout.sessions.core.SessionSetupInstallmentOptions
import org.json.JSONObject

@Suppress("LongParameterList")
fun getPaymentMethodRequest(
    merchantAccount: String,
    shopperReference: String,
    amount: Amount?,
    countryCode: String,
    shopperLocale: String?,
    splitCardFundingSources: Boolean,
    order: OrderRequest? = null,
): PaymentMethodsRequest {
    return PaymentMethodsRequest(
        merchantAccount = merchantAccount,
        shopperReference = shopperReference,
        amount = if (order == null) amount else null,
        countryCode = countryCode,
        shopperLocale = shopperLocale,
        splitCardFundingSources = splitCardFundingSources,
        order = order,
        channel = CHANNEL,
    )
}

@Suppress("LongParameterList")
fun getSessionRequest(
    merchantAccount: String,
    shopperReference: String,
    amount: Amount?,
    countryCode: String,
    shopperLocale: String?,
    splitCardFundingSources: Boolean,
    redirectUrl: String,
    threeDSMode: ThreeDSMode,
    installmentOptions: Map<String, SessionSetupInstallmentOptions>?,
    showInstallmentAmount: Boolean = false,
    showRemovePaymentMethodButton: Boolean = false,
    threeDSAuthenticationOnly: Boolean = false,
    shopperEmail: String? = null,
    allowedPaymentMethods: List<String>? = null,
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
        authenticationData = getAuthenticationData(threeDSMode),
        lineItems = LINE_ITEMS,
        threeDSAuthenticationOnly = threeDSAuthenticationOnly,
        shopperEmail = shopperEmail,
        allowedPaymentMethods = allowedPaymentMethods,
        storePaymentMethodMode = StorePaymentMethodMode.ASK_FOR_CONSENT.mode,
        recurringProcessingModel = recurringProcessingModel,
        installmentOptions = installmentOptions,
        showInstallmentAmount = showInstallmentAmount,
        showRemovePaymentMethodButton = showRemovePaymentMethodButton,
    )
}

@Suppress("LongParameterList")
fun createPaymentRequest(
    paymentComponentData: JSONObject,
    shopperReference: String,
    amount: Amount?,
    countryCode: String,
    merchantAccount: String,
    returnUrl: String,
    threeDSMode: ThreeDSMode,
    shopperEmail: String?,
    threeDSAuthenticationOnly: Boolean = false,
    recurringProcessingModel: String? = RecurringProcessingModel.SUBSCRIPTION.recurringModel,
): PaymentsRequest {
    val paymentsRequestData = PaymentsRequestData(
        shopperReference = shopperReference,
        amount = amount,
        merchantAccount = merchantAccount,
        returnUrl = returnUrl,
        countryCode = countryCode,
        shopperIP = SHOPPER_IP,
        reference = getReference(),
        channel = CHANNEL,
        authenticationData = getAuthenticationData(threeDSMode),
        lineItems = LINE_ITEMS,
        shopperEmail = shopperEmail,
        threeDSAuthenticationOnly = threeDSAuthenticationOnly,
        recurringProcessingModel = recurringProcessingModel,
    )

    return PaymentsRequest(paymentComponentData, paymentsRequestData)
}

fun createBalanceRequest(
    paymentComponentData: JSONObject,
    amount: JSONObject,
    merchantAccount: String,
) = BalanceRequest(
    paymentMethod = paymentComponentData,
    amount = amount,
    merchantAccount = merchantAccount,
)

fun createOrderRequest(
    amount: Amount,
    merchantAccount: String,
) = CreateOrderRequest(
    amount = amount,
    merchantAccount = merchantAccount,
    reference = getReference(),
)

fun createCancelOrderRequest(
    orderJson: JSONObject,
    merchantAccount: String,
) = CancelOrderRequest(
    order = orderJson,
    merchantAccount = merchantAccount,
)

private const val SHOPPER_IP = "142.12.31.22"
private const val CHANNEL = "android"
private val LINE_ITEMS = listOf(Item())
private const val DEFAULT_INSTALLMENT_OPTION = "card"
private const val CARD_BASED_INSTALLMENT_OPTION = "visa"
private const val ATTEMPT_AUTHENTICATION_TRUE_VALUE = "always"
private const val ATTEMPT_AUTHENTICATION_FALSE_VALUE = "never"
private const val THREE_DS_REQUEST_DATA_NATIVE = "preferred"
private const val THREE_DS_REQUEST_DATA_REDIRECT = "disabled"

private fun getReference() = "android-test-${System.currentTimeMillis()}"
private fun getAuthenticationData(threeDSMode: ThreeDSMode): AuthenticationData {
    return when (threeDSMode) {
        ThreeDSMode.PREFER_NATIVE -> AuthenticationData(
            attemptAuthentication = ATTEMPT_AUTHENTICATION_TRUE_VALUE,
            threeDSRequestData = ThreeDSRequestData(THREE_DS_REQUEST_DATA_NATIVE),
        )

        ThreeDSMode.REDIRECT -> AuthenticationData(
            attemptAuthentication = ATTEMPT_AUTHENTICATION_TRUE_VALUE,
            threeDSRequestData = ThreeDSRequestData(THREE_DS_REQUEST_DATA_REDIRECT),
        )

        ThreeDSMode.DISABLED -> AuthenticationData(
            attemptAuthentication = ATTEMPT_AUTHENTICATION_FALSE_VALUE,
            threeDSRequestData = null,
        )
    }
}

fun getSettingsInstallmentOptionsMode(settingsInstallmentOptionMode: CardInstallmentOptionsMode) =
    when (settingsInstallmentOptionMode) {
        CardInstallmentOptionsMode.NONE -> null

        CardInstallmentOptionsMode.DEFAULT -> mapOf(
            DEFAULT_INSTALLMENT_OPTION to getSessionInstallmentOption(),
        )

        CardInstallmentOptionsMode.DEFAULT_WITH_REVOLVING -> mapOf(
            DEFAULT_INSTALLMENT_OPTION to getSessionInstallmentOption(plans = listOf(InstallmentPlan.REVOLVING.plan)),
        )

        CardInstallmentOptionsMode.CARD_BASED_VISA -> mapOf(
            CARD_BASED_INSTALLMENT_OPTION to getSessionInstallmentOption(),
        )
    }

@Suppress("MagicNumber")
private fun getSessionInstallmentOption(
    plans: List<String> = listOf(InstallmentPlan.REGULAR.plan),
    preselectedValue: Int = 2,
    values: List<Int> = listOf(2, 3)
) = SessionSetupInstallmentOptions(plans, preselectedValue, values)
