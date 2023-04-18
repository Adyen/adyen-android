/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/12/2021.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.paymentmethod.GiftCardPaymentMethod
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.core.internal.data.model.getStringOrNull
import com.adyen.checkout.core.internal.data.model.toStringPretty
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.BalanceDropInServiceResult
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.DropInServiceResult
import com.adyen.checkout.dropin.OrderDropInServiceResult
import com.adyen.checkout.dropin.RecurringDropInServiceResult
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
 * In addition, it handles the partial payment flow (gift cards) by implementing [onBalanceCheck],
 * [onOrderRequest] and [onOrderCancel] and it handles the stored payment method removal flow by
 * implementing [onRemoveStoredPaymentMethod].
 */
@Suppress("TooManyFunctions")
@AndroidEntryPoint
class ExampleAdvancedDropInService : DropInService() {

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var recurringRepository: RecurringRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun onSubmit(
        state: PaymentComponentState<*>,
    ) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "onPaymentsCallRequested")

            checkPaymentState(state)
            checkAdditionalData()

            val paymentComponentJson = PaymentComponentData.SERIALIZER.serialize(state.data)
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
            val response = paymentsRepository.makePaymentsRequest(paymentRequest)

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
    @Suppress("UnusedPrivateMember", "UNUSED_VARIABLE")
    private fun checkAdditionalData() {
        val additionalData = getAdditionalData()
        // read bundle and handle it
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "onDetailsCallRequested")

            val actionComponentJson = ActionComponentData.SERIALIZER.serialize(actionComponentData)

            Logger.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

            val response = paymentsRepository.makeDetailsRequest(actionComponentJson)

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
            isRefusedInPartialPaymentFlow(jsonResponse) -> {
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

    private fun isRefusedInPartialPaymentFlow(jsonResponse: JSONObject) =
        isRefused(jsonResponse) && isNonFullyPaidOrder(jsonResponse)

    private fun isRefused(jsonResponse: JSONObject): Boolean {
        return jsonResponse.getStringOrNull("resultCode")
            .equals(other = RESULT_REFUSED, ignoreCase = true)
    }

    private fun isAction(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("action")
    }

    private fun isNonFullyPaidOrder(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("order") && (getOrderFromResponse(jsonResponse).remainingAmount?.value ?: 0) > 0
    }

    private fun getOrderFromResponse(jsonResponse: JSONObject): OrderResponse {
        val orderJSON = jsonResponse.getJSONObject("order")
        return OrderResponse.SERIALIZER.deserialize(orderJSON)
    }

    private fun fetchPaymentMethods(orderResponse: OrderResponse? = null) {
        Logger.d(TAG, "fetchPaymentMethods")
        launch(Dispatchers.IO) {
            val order = orderResponse?.let {
                Order(
                    pspReference = it.pspReference,
                    orderData = it.orderData
                )
            }
            val paymentMethodRequest = getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                order = order
            )
            val paymentMethods = paymentsRepository.getPaymentMethods(paymentMethodRequest)
            val result = if (paymentMethods != null) {
                DropInServiceResult.Update(paymentMethods, orderResponse)
            } else {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
            sendResult(result)
        }
    }

    override fun onBalanceCheck(paymentComponentData: PaymentComponentData<GiftCardPaymentMethod>) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "checkBalance")
            val amount = paymentComponentData.amount
            val paymentMethod = paymentComponentData.paymentMethod
            if (paymentMethod != null && amount != null) {
                val paymentMethodJson = PaymentMethodDetails.SERIALIZER.serialize(paymentMethod)
                val amountJson = Amount.SERIALIZER.serialize(amount)

                val request = createBalanceRequest(
                    paymentMethodJson,
                    amountJson,
                    keyValueStorage.getMerchantAccount()
                )

                val response = paymentsRepository.getBalance(request)
                val result = handleBalanceResponse(response)
                sendBalanceResult(result)
            } else {
                sendBalanceResult(BalanceDropInServiceResult.Error("amount or paymentMethod is null."))
            }
        }
    }

    @Suppress("SwallowedException")
    private fun handleBalanceResponse(jsonResponse: JSONObject?): BalanceDropInServiceResult {
        return if (jsonResponse != null) {
            when (val resultCode = jsonResponse.getStringOrNull("resultCode")) {
                "Success" -> BalanceDropInServiceResult.Balance(BalanceResult.SERIALIZER.deserialize(jsonResponse))
                "NotEnoughBalance" -> {
                    try {
                        BalanceDropInServiceResult.Balance(BalanceResult.SERIALIZER.deserialize(jsonResponse))
                    } catch (e: ModelSerializationException) {
                        BalanceDropInServiceResult.Error(
                            reason = "Not enough balance",
                            dismissDropIn = false
                        )
                    }
                }
                else -> BalanceDropInServiceResult.Error(reason = resultCode, dismissDropIn = false)
            }
        } else {
            Logger.e(TAG, "FAILED")
            BalanceDropInServiceResult.Error(reason = "IOException")
        }
    }

    override fun onOrderRequest() {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "createOrder")

            val paymentRequest = createOrderRequest(
                keyValueStorage.getAmount(),
                keyValueStorage.getMerchantAccount()
            )

            val response = paymentsRepository.createOrder(paymentRequest)

            val result = handleOrderResponse(response)
            sendOrderResult(result)
        }
    }

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

    override fun onOrderCancel(order: Order, shouldUpdatePaymentMethods: Boolean) {
        launch(Dispatchers.IO) {
            Logger.d(TAG, "cancelOrder")
            val orderJson = Order.SERIALIZER.serialize(order)
            val request = createCancelOrderRequest(
                orderJson,
                keyValueStorage.getMerchantAccount()
            )
            val response = paymentsRepository.cancelOrder(request)

            val result = handleCancelOrderResponse(response, shouldUpdatePaymentMethods) ?: return@launch
            sendResult(result)
        }
    }

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

    override fun onRemoveStoredPaymentMethod(
        storedPaymentMethod: StoredPaymentMethod,
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

    companion object {
        private val TAG = LogUtil.getTag()
        private const val RESULT_REFUSED = "refused"
    }
}
