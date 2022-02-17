/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/12/2021.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
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
 *
 * In addition, it handles the partial payment flow (gift cards) by implementing [checkBalance],
 * [createOrder] and [cancelOrder].
 */
@Suppress("TooManyFunctions")
@AndroidEntryPoint
class ExampleFullAsyncDropInService : DropInService() {

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
            checkAdditionalData()

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

            val result = handleResponse(response) ?: return@launch
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

    /**
     * This is an example on how to fetch additional data
     */
    private fun checkAdditionalData() {
        val additionalData = getAdditionalData()
        // read bundle and handle it
    }

    override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "onDetailsCallRequested")

            Logger.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

            val requestBody = actionComponentJson.toString().toRequestBody(CONTENT_TYPE)
            val response = paymentsRepository.detailsRequestAsync(requestBody)

            val result = handleResponse(response) ?: return@launch
            sendResult(result)
        }
    }

    private fun handleResponse(response: ResponseBody?): DropInServiceResult? {
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
            isNonFullyPaidOrder(jsonResponse) -> {
                Logger.d(TAG, "Received a non fully paid order")
                val order = getOrderFromResponse(jsonResponse)
                fetchPaymentMethods(order)
                null
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

    private fun isNonFullyPaidOrder(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("order") && getOrderFromResponse(jsonResponse).remainingAmount?.value ?: 0 > 0
    }

    private fun getOrderFromResponse(jsonResponse: JSONObject): OrderResponse {
        val orderJSON = jsonResponse.getJSONObject("order")
        return OrderResponse.SERIALIZER.deserialize(orderJSON)
    }

    private fun fetchPaymentMethods(order: OrderResponse? = null) {
        Logger.d(TAG, "fetchPaymentMethods")
        launch(Dispatchers.IO) {
            val orderRequest = if (order == null) null else OrderRequest(
                pspReference = order.pspReference,
                orderData = order.orderData
            )
            val paymentMethodRequest = getPaymentMethodRequest(keyValueStorage, orderRequest)
            val paymentMethods = paymentsRepository.getPaymentMethods(paymentMethodRequest)
            val result = if (paymentMethods != null) {
                DropInServiceResult.Update(paymentMethods, order)
            } else {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
            sendResult(result)
        }
    }

    override fun checkBalance(paymentMethodData: PaymentMethodDetails) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "checkBalance")

            val paymentMethodJson = PaymentMethodDetails.SERIALIZER.serialize(paymentMethodData)
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

    override fun cancelOrder(order: OrderRequest, shouldUpdatePaymentMethods: Boolean) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "cancelOrder")
            val orderJson = OrderRequest.SERIALIZER.serialize(order)
            val cancelOrderRequest = createCancelOrderRequest(
                orderJson,
                keyValueStorage.getMerchantAccount()
            )
            val requestBody = cancelOrderRequest.toString().toRequestBody(CONTENT_TYPE)
            val response = paymentsRepository.cancelOrderAsync(requestBody)

            val result = handleCancelOrderResponse(response, shouldUpdatePaymentMethods) ?: return@launch
            sendResult(result)
        }
    }

    @Suppress("NestedBlockDepth")
    private fun handleCancelOrderResponse(response: ResponseBody?, shouldUpdatePaymentMethods: Boolean): DropInServiceResult? {
        return if (response != null) {
            val orderJson = response.string()
            val jsonResponse = JSONObject(orderJson)
            Logger.v(TAG, "cancelOrder response - ${jsonResponse.toStringPretty()}")
            val resultCode = jsonResponse.getStringOrNull("resultCode")
            when (resultCode) {
                "Received" -> {
                    if (shouldUpdatePaymentMethods) fetchPaymentMethods()
                    null
                }
                else -> DropInServiceResult.Error(reason = resultCode, dismissDropIn = false)
            }
        } else {
            Logger.e(TAG, "FAILED")
            DropInServiceResult.Error(reason = "IOException")
        }
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
