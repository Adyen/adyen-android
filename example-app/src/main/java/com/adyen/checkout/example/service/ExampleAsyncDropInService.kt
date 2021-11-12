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
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.BalanceResult
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.BalanceDropInServiceResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.OrderDropInServiceResult
import com.adyen.checkout.example.data.api.model.paymentsRequest.AdditionalData
import com.adyen.checkout.example.data.storage.KeyValueStorage
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
        return if (response != null) {
            val detailsResponse = JSONObject(response.string())
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
            Logger.e(TAG, "FAILED")
            DropInServiceResult.Error(reason = "IOException")
        }
    }

    override fun checkBalance(paymentMethodData: PaymentMethodDetails, paymentMethodJson: JSONObject) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "checkBalance")

            Logger.v(TAG, "paymentMethods/balance/ - ${paymentMethodJson.toStringPretty()}")

            val paymentRequest = createBalanceRequest(
                paymentMethodJson,
                keyValueStorage.getMerchantAccount()
            )

            val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)
            val response = paymentsRepository.balanceRequestAsync(requestBody)
            val result = handleBalanceResponse(response)
            sendBalanceResult(result)
        }
    }

    private fun handleBalanceResponse(response: ResponseBody?): BalanceDropInServiceResult {
        return if (response != null) {
            val balanceJson = response.string()
            val jsonResponse = JSONObject(balanceJson)
            val resultCode = jsonResponse.getStringOrNull("resultCode")
            when (resultCode) {
                "Success" -> BalanceDropInServiceResult.Balance(BalanceResult.SERIALIZER.deserialize(jsonResponse))
                "NotEnoughBalance" -> BalanceDropInServiceResult.Error(reason = "Not enough balance", dismissDropIn = false)
                else -> BalanceDropInServiceResult.Error(reason = resultCode, dismissDropIn = false)
            }
        } else {
            Logger.e(TAG, "FAILED")
            BalanceDropInServiceResult.Error(reason = "IOException")
        }
    }

    override fun createOrder() {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "createOrder")

            val paymentRequest = createOrderRequest(
                keyValueStorage.getAmount(),
                keyValueStorage.getMerchantAccount()
            )

            val requestBody = paymentRequest.toString().toRequestBody(CONTENT_TYPE)
            val response = paymentsRepository.createOrderAsync(requestBody)

            val result = handleOrderResponse(response)
            sendOrderResult(result)
        }
    }

    @Suppress("NestedBlockDepth")
    private fun handleOrderResponse(response: ResponseBody?): OrderDropInServiceResult {
        return if (response != null) {
            val orderJson = response.string()
            val jsonResponse = JSONObject(orderJson)
            val resultCode = jsonResponse.getStringOrNull("resultCode")
            when (resultCode) {
                "Success" -> OrderDropInServiceResult.OrderCreated(OrderResponse.SERIALIZER.deserialize(jsonResponse))
                else -> OrderDropInServiceResult.Error(reason = resultCode, dismissDropIn = false)
            }
        } else {
            Logger.e(TAG, "FAILED")
            OrderDropInServiceResult.Error(reason = "IOException")
        }
    }
}
