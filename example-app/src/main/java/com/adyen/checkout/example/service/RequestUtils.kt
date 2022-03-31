/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 13/12/2019.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.example.data.api.model.AdditionalData
import com.adyen.checkout.example.data.api.model.Item
import com.adyen.checkout.example.data.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.data.api.model.SessionRequest
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type

fun getPaymentMethodRequest(keyValueStorage: KeyValueStorage, order: OrderRequest? = null): PaymentMethodsRequest {
    return PaymentMethodsRequest(
        merchantAccount = keyValueStorage.getMerchantAccount(),
        shopperReference = keyValueStorage.getShopperReference(),
        amount = if (order == null) keyValueStorage.getAmount() else null,
        countryCode = keyValueStorage.getCountry(),
        shopperLocale = keyValueStorage.getShopperLocale(),
        splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
        order = order
    )
}

fun getSessionRequest(keyValueStorage: KeyValueStorage, returnUrl: String): SessionRequest {
    return SessionRequest(
        merchantAccount = keyValueStorage.getMerchantAccount(),
        shopperReference = keyValueStorage.getShopperReference(),
        amount = keyValueStorage.getAmount(),
        countryCode = keyValueStorage.getCountry(),
        shopperLocale = keyValueStorage.getShopperLocale(),
        splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
        returnUrl = returnUrl
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
    additionalData: AdditionalData,
    force3DS2Challenge: Boolean = true,
    threeDSAuthenticationOnly: Boolean = false
) = JSONObject(paymentComponentData.toString()).apply {
    put("shopperReference", shopperReference)
    if (!has("amount")) put("amount", JSONObject(getJsonAdapter(Amount::class.java).toJson(amount)))
    put("merchantAccount", merchantAccount)
    put("returnUrl", redirectUrl)
    put("countryCode", countryCode)
    put("shopperIP", "142.12.31.22")
    put("reference", "android-test-components_${System.currentTimeMillis()}")
    put("channel", "android")
    put("additionalData", JSONObject(getJsonAdapter(AdditionalData::class.java).toJson(additionalData)))
    val listItemJsonAdapter = getJsonAdapter<List<Item>>(Types.newParameterizedType(List::class.java, Item::class.java))
    put("lineItems", JSONArray(listItemJsonAdapter.toJson(listOf(Item()))))
    put("threeDSAuthenticationOnly", threeDSAuthenticationOnly)

    if (force3DS2Challenge) {
        val threeDS2RequestData = JSONObject()
        threeDS2RequestData.put("deviceChannel", "app")
        threeDS2RequestData.put("challengeIndicator", "requestChallenge")
        put("threeDS2RequestData", threeDS2RequestData)
    }
}

fun createBalanceRequest(
    paymentComponentData: JSONObject,
    merchantAccount: String,
): JSONObject {
    return JSONObject().apply {
        put("paymentMethod", paymentComponentData)
        put("merchantAccount", merchantAccount)
    }
}

fun createOrderRequest(
    amount: Amount,
    merchantAccount: String
) = JSONObject().apply {
    put("amount", JSONObject(getJsonAdapter(Amount::class.java).toJson(amount)))
    put("merchantAccount", merchantAccount)
    put("reference", "android-test-components_${System.currentTimeMillis()}")
}

fun createCancelOrderRequest(
    orderJson: JSONObject,
    merchantAccount: String
) = JSONObject().apply {
    put("order", orderJson)
    put("merchantAccount", merchantAccount)
}

fun createRemoveStoredPaymentMethodRequest(
    recurringDetailReference: String,
    merchantAccount: String,
    shopperReference: String
) = JSONObject().apply {
    put("recurringDetailReference", recurringDetailReference)
    put("merchantAccount", merchantAccount)
    put("shopperReference", shopperReference)
}

private fun <T> getJsonAdapter(clazz: Class<T>): JsonAdapter<T> {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    return moshi.adapter(clazz)
}

private fun <T> getJsonAdapter(type: Type): JsonAdapter<T> {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    return moshi.adapter(type)
}
