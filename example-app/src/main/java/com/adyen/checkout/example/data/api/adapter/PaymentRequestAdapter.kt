/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/3/2022.
 */

package com.adyen.checkout.example.data.api.adapter

import com.adyen.checkout.example.data.api.model.PaymentsRequest
import com.adyen.checkout.example.data.api.model.PaymentsRequestData
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject

class PaymentRequestAdapter {
    @FromJson
    fun fromJson(jsonObject: JSONObject?): PaymentsRequest? {
        throw UnsupportedOperationException("PaymentsRequest can only be serialized")
    }

    @ToJson
    fun toJson(paymentsRequest: PaymentsRequest?): JSONObject? {
        if (paymentsRequest == null) return null
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(PaymentsRequestData::class.java)
        val requestDataJson = JSONObject(adapter.toJson(paymentsRequest.requestData))
        val mergedJSON = JSONObject().apply {
            putAll(requestDataJson)
            // paymentComponentData should be merged second to override any possible duplicated fields
            putAll(paymentsRequest.paymentComponentData)
        }
        return mergedJSON
    }

    private fun JSONObject.putAll(other: JSONObject) {
        val keys = other.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = other.get(key)
            put(key, value)
        }
    }
}
