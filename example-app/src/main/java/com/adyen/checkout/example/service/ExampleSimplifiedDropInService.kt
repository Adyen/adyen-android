/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/10/2019.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.dropin.service.SimplifiedDropInService
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Call

class ExampleSimplifiedDropInService : SimplifiedDropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
        private val CONTENT_TYPE: MediaType = "application/json".toMediaType()
    }

    private val paymentsRepository: PaymentsRepository by inject()
    private val keyValueStorage: KeyValueStorage by inject()

    override fun makePaymentsCallOrFail(paymentComponentData: JSONObject): JSONObject? {
        Logger.d(TAG, "makePaymentsCallOrFail")
        Logger.v(TAG, "paymentComponentData - ${JsonUtils.indent(paymentComponentData)}")

        // Check out the documentation of this method on the parent DropInService class
        val paymentRequest = createPaymentRequest(
            paymentComponentData,
            keyValueStorage.getShopperReference(),
            keyValueStorage.getAmount(),
            keyValueStorage.getCountry(),
            keyValueStorage.getMerchantAccount(),
            RedirectComponent.getReturnUrl(applicationContext),
            AdditionalData(
                allow3DS2 = keyValueStorage.isThreeds2Enable().toString(),
                executeThreeD = keyValueStorage.isExecuteThreeD().toString()
            )
        )

        val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)
        val call = paymentsRepository.paymentsRequest(requestBody)

        return makeCall(call)
    }

    override fun makeDetailsCallOrFail(actionComponentData: JSONObject): JSONObject? {
        Logger.d(TAG, "makeDetailsCallOrFail")
        Logger.v(TAG, "actionComponentData - ${JsonUtils.indent(actionComponentData)}")

        val requestBody = actionComponentData.toString().toRequestBody(CONTENT_TYPE)
        val call = paymentsRepository.detailsRequest(requestBody)

        return makeCall(call)
    }

    private fun makeCall(call: Call<ResponseBody>): JSONObject? {
        val response = call.execute()

        val byteArray = response.errorBody()?.bytes()
        if (byteArray != null) {
            Logger.e(TAG, "errorBody - ${String(byteArray)}")
        }

        if (response.isSuccessful) {
            return JSONObject(response.body()?.string())
        }

        return null
    }
}
