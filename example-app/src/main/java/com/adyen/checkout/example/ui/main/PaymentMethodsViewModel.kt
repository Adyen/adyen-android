/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.api.model.paymentsRequest.PaymentMethodsRequest
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class PaymentMethodsViewModel(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage
) : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
        private val CONTENT_TYPE: MediaType = "application/json".toMediaType()
    }

    private val parentJob = Job()

    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    val paymentMethodResponseLiveData = MutableLiveData<PaymentMethodsApiResponse>()

    fun requestPaymentMethods() {
        scope.launch {
            paymentMethodResponseLiveData.postValue(paymentsRepository.getPaymentMethods(getPaymentMethodRequest()))
        }
    }

    private fun getPaymentMethodRequest(): PaymentMethodsRequest {
        return PaymentMethodsRequest(
            keyValueStorage.getMerchantAccount(),
            keyValueStorage.getShopperReference(),
            keyValueStorage.getAmount(),
            keyValueStorage.getCountry(),
            keyValueStorage.getShopperLocale()
        )
    }

    fun makePaymentsCall(paymentComponentData: JSONObject, returnUrl: String) {
        scope.launch {
            Logger.d(TAG, "makePaymentsCall")

            // Check out the documentation of this method on the parent DropInService class
            val paymentRequest = createPaymentRequest(
                paymentComponentData,
                keyValueStorage.getShopperReference(),
                keyValueStorage.getAmount(),
                keyValueStorage.getCountry(),
                keyValueStorage.getMerchantAccount(),
                returnUrl,
                AdditionalData(
                    allow3DS2 = keyValueStorage.isThreeds2Enable().toString(),
                    executeThreeD = keyValueStorage.isExecuteThreeD().toString()
                )
            )

            Logger.v(TAG, "paymentComponentData - ${JsonUtils.indent(paymentComponentData)}")

            val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)
            val call = paymentsRepository.paymentsRequestAsync(requestBody)

            handleResponse(call)
        }
    }

    fun makeDetailsCall(actionComponentData: JSONObject) {
        scope.launch {
            Logger.d(TAG, "makeDetailsCall")

            Logger.v(TAG, "payments/details/ - ${JsonUtils.indent(actionComponentData)}")

            val requestBody = actionComponentData.toString().toRequestBody(CONTENT_TYPE)
            val call = paymentsRepository.detailsRequestAsync(requestBody)

            handleResponse(call)
        }
    }

    @Suppress("NestedBlockDepth")
    private fun handleResponse(response: ResponseBody?) {
        val result = try {
            if (response != null) {
                val detailsResponse = JSONObject(response.string())
                if (detailsResponse.has("action")) {
                    DropInServiceResult.Action(detailsResponse.get("action").toString())
                } else {
                    Logger.d(TAG, "Final result - ${JsonUtils.indent(detailsResponse)}")

                    val resultCode = if (detailsResponse.has("resultCode")) {
                        detailsResponse.get("resultCode").toString()
                    } else {
                        "EMPTY"
                    }
                    DropInServiceResult.Finished(resultCode)
                }
            } else {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            DropInServiceResult.Error(reason = "IOException")
        }

        DropIn.sendResult(result)
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        coroutineContext.cancel()
    }
}
