/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * This is just an example on how to make networkModule calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 *
 * This class implements [makePaymentsCall] and [makeDetailsCall] which provide the simplest way of
 * interacting with Drop-in, without having to manage background threads.
 */
@AndroidEntryPoint
class ExampleDropInService : DropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
        private val CONTENT_TYPE: MediaType = "application/json".toMediaType()
    }

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun makePaymentsCall(paymentComponentJson: JSONObject): DropInServiceResult {
        Logger.d(TAG, "makePaymentsCall")

        // Check out the documentation of this method on the parent DropInService class
        val paymentRequest = createPaymentRequest(
            paymentComponentJson,
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

        Logger.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")

        val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)
        val call = paymentsRepository.paymentsRequest(requestBody)

        return handleResponse(call)
    }

    override fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
        Logger.d(TAG, "makeDetailsCall")

        Logger.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

        val requestBody = actionComponentJson.toString().toRequestBody(CONTENT_TYPE)
        val call = paymentsRepository.detailsRequest(requestBody)

        return handleResponse(call)
    }

    @Suppress("NestedBlockDepth")
    private fun handleResponse(call: Call<ResponseBody>): DropInServiceResult {
        return try {
            val response = call.execute()

            val byteArray = response.errorBody()?.bytes()
            if (byteArray != null) {
                Logger.e(TAG, "errorBody - ${String(byteArray)}")
            }

            if (response.isSuccessful) {
                val detailsResponse = JSONObject(response.body()?.string() ?: "")
                if (detailsResponse.has("action")) {
                    val action = Action.SERIALIZER.deserialize(detailsResponse.getJSONObject("action"))
                    DropInServiceResult.Action(action)
                } else {
                    Logger.d(TAG, "Final result - ${detailsResponse.toStringPretty()}")

                    val resultCode = if (detailsResponse.has("resultCode")) {
                        detailsResponse.get("resultCode").toString()
                    } else {
                        "EMPTY"
                    }
                    DropInServiceResult.Finished(resultCode)
                }
            } else {
                Logger.e(TAG, "FAILED - ${response.message()}")
                DropInServiceResult.Error(reason = "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            DropInServiceResult.Error(reason = "IOException")
        }
    }
}
