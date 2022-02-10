/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/1/2021.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.RecurringDropInServiceResult
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.RecurringRepository
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 * This is just an example on how to make networkModule calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 *
 * This class implements [onPaymentsCallRequested] and [onDetailsCallRequested] which provide more
 * freedom in handling the API calls, managing threads and checking component states.
 */
@AndroidEntryPoint
class ExampleAsyncDropInService : DropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
        private val CONTENT_TYPE: MediaType = "application/json".toMediaType()
    }

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var recurringRepository: RecurringRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun onPaymentsCallRequested(paymentComponentState: PaymentComponentState<*>, paymentComponentJson: JSONObject) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "onPaymentsCallRequested")

            checkPaymentState(paymentComponentState)

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
            val response = paymentsRepository.paymentsRequestAsync(requestBody)

            val result = handleResponse(response)
            sendResult(result)
        }
    }

    /**
     * This is an example on how to handle the PaymentComponentState
     */
    private fun checkPaymentState(paymentComponentState: PaymentComponentState<*>) {
        if (paymentComponentState is CardComponentState) {
            // a card payment is being made, handle accordingly
        }
    }

    override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "onDetailsCallRequested")

            Logger.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

            val requestBody = actionComponentJson.toString().toRequestBody(CONTENT_TYPE)
            val response = paymentsRepository.detailsRequestAsync(requestBody)

            val result = handleResponse(response)
            sendResult(result)
        }
    }

    private fun handleResponse(response: ResponseBody?): DropInServiceResult {
        val jsonResponse = if (response == null) null else JSONObject(response.string())
        return when {
            jsonResponse == null -> {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
            isAction(jsonResponse) -> {
                Logger.d(TAG, "Received action")
                val action = Action.SERIALIZER.deserialize(jsonResponse.getJSONObject("action"))
                DropInServiceResult.Action(action)
            }
            else -> {
                Logger.d(TAG, "Final result - ${jsonResponse.toStringPretty()}")
                val resultCode = if (jsonResponse.has("resultCode")) {
                    jsonResponse.get("resultCode").toString()
                } else {
                    "EMPTY"
                }
                DropInServiceResult.Finished(resultCode)
            }
        }
    }

    private fun isAction(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("action")
    }

    override fun removeStoredPaymentMethod(
        storedPaymentMethod: StoredPaymentMethod,
        storedPaymentMethodJson: JSONObject
    ) {
        launch(Dispatchers.IO) {
            val requestBody = createRemoveStoredPaymentMethodRequest(
                storedPaymentMethod.id.orEmpty(),
                keyValueStorage.getMerchantAccount(),
                keyValueStorage.getShopperReference()
            ).toString().toRequestBody(CONTENT_TYPE)
            val response = recurringRepository.removeStoredPaymentMethod(requestBody)
            val result = handleRemoveStoredPaymentMethodResult(response, storedPaymentMethod.id.orEmpty())
            sendRecurringResult(result)
        }
    }

    private fun handleRemoveStoredPaymentMethodResult(response: ResponseBody?, id: String): RecurringDropInServiceResult {
        return if (response != null) {
            val orderJson = response.string()
            val jsonResponse = JSONObject(orderJson)
            Logger.v(TAG, "removeStoredPaymentMethod response - ${jsonResponse.toStringPretty()}")
            val responseCode = jsonResponse.getStringOrNull("response")
            when (responseCode) {
                "[detail-successfully-disabled]" -> RecurringDropInServiceResult.PaymentMethodRemoved(id)
                else -> RecurringDropInServiceResult.Error(reason = responseCode, dismissDropIn = false)
            }
        } else {
            Logger.e(TAG, "FAILED")
            RecurringDropInServiceResult.Error(reason = "IOException")
        }
    }
}
