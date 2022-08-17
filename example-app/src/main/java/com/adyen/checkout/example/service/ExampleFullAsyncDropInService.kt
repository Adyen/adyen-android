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
import com.adyen.checkout.components.status.api.StatusResponseUtils
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.BalanceDropInServiceResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.OrderDropInServiceResult
import com.adyen.checkout.dropin.service.RecurringDropInServiceResult
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.repositories.RecurringRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

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
    }

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var recurringRepository: RecurringRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "onPaymentsCallRequested")

            checkPaymentState(paymentComponentState)
            checkAdditionalData()

            // Check out the documentation of this method on the parent DropInService class
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentJson,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = RedirectComponent.getReturnUrl(applicationContext),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
                shopperEmail = keyValueStorage.getShopperEmail()
            )

            Logger.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")
            val response = paymentsRepository.paymentsRequestAsync(paymentRequest)

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

            val response = paymentsRepository.detailsRequestAsync(actionComponentJson)

            val result = handleResponse(response) ?: return@launch
            sendResult(result)
        }
    }

    private fun handleResponse(jsonResponse: JSONObject?): DropInServiceResult? {
        return when {
            jsonResponse == null -> {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
            isRefused(jsonResponse) -> {
                Logger.d(TAG, "Refused")
                DropInServiceResult.Error(reason = "Refused")
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

    private fun isRefused(jsonResponse: JSONObject): Boolean {
        return jsonResponse.getStringOrNull("resultCode")
            .equals(other = StatusResponseUtils.RESULT_REFUSED, ignoreCase = true)
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
            val paymentMethodRequest = getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                order = orderRequest
            )
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

            val request = createBalanceRequest(
                paymentMethodJson,
                keyValueStorage.getMerchantAccount()
            )

            val response = paymentsRepository.balanceRequestAsync(request)
            val result = handleBalanceResponse(response)
            sendBalanceResult(result)
        }
    }

    private fun handleBalanceResponse(jsonResponse: JSONObject?): BalanceDropInServiceResult {
        return if (jsonResponse != null) {
            when (val resultCode = jsonResponse.getStringOrNull("resultCode")) {
                "Success" -> BalanceDropInServiceResult.Balance(BalanceResult.SERIALIZER.deserialize(jsonResponse))
                "NotEnoughBalance" -> BalanceDropInServiceResult.Error(
                    reason = "Not enough balance",
                    dismissDropIn = false
                )
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

            val response = paymentsRepository.createOrderAsync(paymentRequest)

            val result = handleOrderResponse(response)
            sendOrderResult(result)
        }
    }

    @Suppress("NestedBlockDepth")
    private fun handleOrderResponse(jsonResponse: JSONObject?): OrderDropInServiceResult {
        return if (jsonResponse != null) {
            when (val resultCode = jsonResponse.getStringOrNull("resultCode")) {
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
            val request = createCancelOrderRequest(
                orderJson,
                keyValueStorage.getMerchantAccount()
            )
            val response = paymentsRepository.cancelOrderAsync(request)

            val result = handleCancelOrderResponse(response, shouldUpdatePaymentMethods) ?: return@launch
            sendResult(result)
        }
    }

    @Suppress("NestedBlockDepth")
    private fun handleCancelOrderResponse(
        jsonResponse: JSONObject?,
        shouldUpdatePaymentMethods: Boolean
    ): DropInServiceResult? {
        return if (jsonResponse != null) {
            Logger.v(TAG, "cancelOrder response - ${jsonResponse.toStringPretty()}")
            when (val resultCode = jsonResponse.getStringOrNull("resultCode")) {
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
            val request = createRemoveStoredPaymentMethodRequest(
                storedPaymentMethod.id.orEmpty(),
                keyValueStorage.getMerchantAccount(),
                keyValueStorage.getShopperReference()
            )
            val response = recurringRepository.removeStoredPaymentMethod(request)
            val result = handleRemoveStoredPaymentMethodResult(response, storedPaymentMethod.id.orEmpty())
            sendRecurringResult(result)
        }
    }

    private fun handleRemoveStoredPaymentMethodResult(
        jsonResponse: JSONObject?,
        id: String
    ): RecurringDropInServiceResult {
        return if (jsonResponse != null) {
            Logger.v(TAG, "removeStoredPaymentMethod response - ${jsonResponse.toStringPretty()}")
            when (val responseCode = jsonResponse.getStringOrNull("response")) {
                "[detail-successfully-disabled]" -> RecurringDropInServiceResult.PaymentMethodRemoved(id)
                else -> RecurringDropInServiceResult.Error(reason = responseCode, dismissDropIn = false)
            }
        } else {
            Logger.e(TAG, "FAILED")
            RecurringDropInServiceResult.Error(reason = "IOException")
        }
    }
}
