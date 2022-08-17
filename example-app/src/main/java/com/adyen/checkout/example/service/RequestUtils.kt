/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 13/12/2019.
 */

@file:Suppress("TooManyFunctions")

package com.adyen.checkout.example.service

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.example.data.api.model.AdditionalData
import com.adyen.checkout.example.data.api.model.BalanceRequest
import com.adyen.checkout.example.data.api.model.CancelOrderRequest
import com.adyen.checkout.example.data.api.model.CreateOrderRequest
import com.adyen.checkout.example.data.api.model.Item
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequestData
import com.adyen.checkout.example.data.api.model.RemoveStoredPaymentMethodRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.example.data.api.model.ThreeDS2RequestDataRequest
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
    force3DS2Challenge: Boolean = true,
    threeDSAuthenticationOnly: Boolean = false,
    shopperEmail: String? = null
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
        shopperEmail = shopperEmail
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
    threeDSAuthenticationOnly: Boolean = false
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
        threeDS2RequestData = if (force3DS2Challenge) ThreeDS2RequestDataRequest() else null
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
private fun getReference() = "android-test-components_${System.currentTimeMillis()}"
private fun getAdditionalData(isThreeds2Enabled: Boolean, isExecuteThreeD: Boolean) = AdditionalData(
    allow3DS2 = isThreeds2Enabled.toString(),
    executeThreeD = isExecuteThreeD.toString()
)
