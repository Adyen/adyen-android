/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call
import java.io.IOException

/**
 * This is just an example on how to make networkModule calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 */
class ExampleDropInService : DropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
        private val CONTENT_TYPE: MediaType = "application/json".toMediaType()
    }

    private val paymentsRepository: PaymentsRepository by inject()
    private val keyValueStorage: KeyValueStorage by inject()

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        Logger.d(TAG, "makePaymentsCall")

        // Check out the documentation of this method on the parent DropInService class
        val paymentRequest = createPaymentRequest(
            paymentComponentData,
            keyValueStorage.getShopperReference(),
            keyValueStorage.getAmount(),
            keyValueStorage.getMerchantAccount(),
            RedirectComponent.getReturnUrl(applicationContext),
            AdditionalData(allow3DS2 = keyValueStorage.isThreeds2Enable().toString()))

        Logger.v(TAG, "paymentComponentData - ${paymentComponentData.toString(JsonUtils.IDENT_SPACES)}")

        val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)
        val call = paymentsRepository.paymentsRequest(requestBody)

        return handleResponse(call)
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        Logger.d(TAG, "makeDetailsCall")

        Logger.v(TAG, "payments/details/ - ${actionComponentData.toString(JsonUtils.IDENT_SPACES)}")

        val requestBody = actionComponentData.toString().toRequestBody(CONTENT_TYPE)
        val call = paymentsRepository.detailsRequest(requestBody)

        return handleResponse(call)
    }

    @Suppress("NestedBlockDepth")
    private fun handleResponse(call: Call<ResponseBody>): CallResult {
        return try {
            val response = call.execute()

            val byteArray = response.errorBody()?.bytes()
            if (byteArray != null) {
                Logger.e(TAG, "errorBody - ${String(byteArray)}")
            }

            val detailsResponse = JSONObject(response.body()?.string())

            if (response.isSuccessful) {
                if (detailsResponse.has("action")) {
                    CallResult(CallResult.ResultType.ACTION, detailsResponse.get("action").toString())
                } else {
                    Logger.d(TAG, "Final result - ${detailsResponse.toString(JsonUtils.IDENT_SPACES)}")

                    var content = "EMPTY"
                    if (detailsResponse.has("resultCode")) {
                        content = detailsResponse.get("resultCode").toString()
                    }
                    CallResult(CallResult.ResultType.FINISHED, content)
                }
            } else {
                Logger.e(TAG, "FAILED - ${response.message()}")
                CallResult(CallResult.ResultType.ERROR, "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            CallResult(CallResult.ResultType.ERROR, "IOException")
        }
    }

    @Suppress("LongParameterList")
    private fun createPaymentRequest(
        paymentComponentData: JSONObject,
        shopperReference: String,
        amount: Amount,
        merchantAccount: String,
        redirectUrl: String,
        additionalData: AdditionalData
    ): JSONObject {

        val request = JSONObject()

        request.putOpt("paymentMethod", paymentComponentData.getJSONObject("paymentMethod"))
        request.put("shopperReference", shopperReference)
        request.put("storePaymentMethod", paymentComponentData.getBoolean("storePaymentMethod"))
        request.put("amount", JSONObject(Gson().toJson(amount)))
        request.put("merchantAccount", merchantAccount)
        request.put("returnUrl", redirectUrl)
        request.put("reference", "android-test-components")
        request.put("channel", "android")
        request.put("additionalData", JSONObject(Gson().toJson(additionalData)))

        return request
    }
}
