/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 13/12/2019.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.api.model.paymentsRequest.Item
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

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
): JSONObject {

    return JSONObject(paymentComponentData.toString()).apply {
        put("shopperReference", shopperReference)
        put("amount", JSONObject(Gson().toJson(amount)))
        put("merchantAccount", merchantAccount)
        put("returnUrl", redirectUrl)
        put("countryCode", countryCode)
        put("shopperIP", "142.12.31.22")
        put("reference", "android-test-components_${System.currentTimeMillis()}")
        put("channel", "android")
        put("additionalData", JSONObject(Gson().toJson(additionalData)))
        put("lineItems", JSONArray(Gson().toJson(listOf(Item()))))
        put("threeDSAuthenticationOnly", threeDSAuthenticationOnly)

        if (force3DS2Challenge) {
            val threeDS2RequestData = JSONObject()
            threeDS2RequestData.put("deviceChannel", "app")
            threeDS2RequestData.put("challengeIndicator", "requestChallenge")
            put("threeDS2RequestData", threeDS2RequestData)
        }
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
