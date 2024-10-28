/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/12/2021.
 */

package com.adyen.checkout.example.service

import android.util.Log
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.dropin.AddressLookupDropInServiceResult
import com.adyen.checkout.dropin.BalanceDropInServiceResult
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.DropInServiceResult
import com.adyen.checkout.dropin.ErrorDialog
import com.adyen.checkout.dropin.FinishedDialog
import com.adyen.checkout.dropin.OrderDropInServiceResult
import com.adyen.checkout.dropin.RecurringDropInServiceResult
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.extensions.toStringPretty
import com.adyen.checkout.example.repositories.AddressLookupCompletionState
import com.adyen.checkout.example.repositories.AddressLookupRepository
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    lateinit var keyValueStorage: KeyValueStorage

    @Inject
    lateinit var addressLookupRepository: AddressLookupRepository

    override fun onCreate() {
        super.onCreate()
        addressLookupRepository.addressLookupOptionsFlow
            .onEach { options ->
                sendAddressLookupResult(AddressLookupDropInServiceResult.LookupResult(options))
            }.launchIn(this)

        addressLookupRepository.addressLookupCompletionFlow
            .onEach {
                val result = when (it) {
                    is AddressLookupCompletionState.Address -> {
                        AddressLookupDropInServiceResult.LookupComplete(it.lookupAddress)
                    }

                    is AddressLookupCompletionState.Error -> AddressLookupDropInServiceResult.Error(
                        errorDialog = ErrorDialog(
                            message = it.message,
                        ),
                    )
                }
                sendAddressLookupResult(result)
            }.launchIn(this)
    }

    @Suppress("RestrictedApi")
    override fun onSubmit(
        state: PaymentComponentState<*>,
    ) {
        launch(DispatcherProvider.IO) {
            Log.d(TAG, "onPaymentsCallRequested")

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
                threeDSMode = keyValueStorage.getThreeDSMode(),
                shopperEmail = keyValueStorage.getShopperEmail(),
            )

            Log.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")
            val response = paymentsRepository.makePaymentsRequest(paymentRequest)

            val result = handleResponse(response) ?: return@launch
            sendResult(result)
        }
    }

    /**
     * This is an example on how to handle the PaymentComponentState
     */
    private fun checkPaymentState(paymentComponentState: PaymentComponentState<*>) {
        @Suppress("ControlFlowWithEmptyBody")
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

    @Suppress("RestrictedApi")
    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        launch(DispatcherProvider.IO) {
            Log.d(TAG, "onDetailsCallRequested")

            val actionComponentJson = ActionComponentData.SERIALIZER.serialize(actionComponentData)

            Log.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

            val response = paymentsRepository.makeDetailsRequest(actionComponentJson)

            val result = handleResponse(response) ?: return@launch
            sendResult(result)
        }
    }

    private fun handleResponse(jsonResponse: JSONObject?): DropInServiceResult? {
        return when {
            jsonResponse == null -> {
                Log.e(TAG, "FAILED")
                DropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
            }

            isRefusedInPartialPaymentFlow(jsonResponse) -> {
                Log.d(TAG, "Refused")
                DropInServiceResult.Error(errorDialog = ErrorDialog(message = "Refused"))
            }

            isAction(jsonResponse) -> {
                Log.d(TAG, "Received action")
                val action = Action.SERIALIZER.deserialize(jsonResponse.getJSONObject("action"))
                DropInServiceResult.Action(action)
            }

            isNonFullyPaidOrder(jsonResponse) -> {
                Log.d(TAG, "Received a non fully paid order")
                val order = getOrderFromResponse(jsonResponse)
                fetchPaymentMethods(order)
                null
            }

            else -> {
                Log.d(TAG, "Final result - ${jsonResponse.toStringPretty()}")
                val resultCode = if (jsonResponse.has("resultCode")) {
                    jsonResponse.get("resultCode").toString()
                } else {
                    "EMPTY"
                }
                DropInServiceResult.Finished(resultCode, FinishedDialog("Payment finished", "Status: $resultCode"))
            }
        }
    }

    private fun isRefusedInPartialPaymentFlow(jsonResponse: JSONObject) =
        isRefused(jsonResponse) && isNonFullyPaidOrder(jsonResponse)

    private fun isRefused(jsonResponse: JSONObject): Boolean {
        return jsonResponse.optString("resultCode")
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

    @Suppress("RestrictedApi")
    private fun fetchPaymentMethods(orderResponse: OrderResponse? = null) {
        Log.d(TAG, "fetchPaymentMethods")
        launch(DispatcherProvider.IO) {
            val order = orderResponse?.let {
                Order(
                    pspReference = it.pspReference,
                    orderData = it.orderData,
                )
            }
            val paymentMethodRequest = getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                order = order,
            )
            val paymentMethods = paymentsRepository.getPaymentMethods(paymentMethodRequest)
            val result = if (paymentMethods != null) {
                DropInServiceResult.Update(paymentMethods, orderResponse)
            } else {
                Log.e(TAG, "FAILED")
                DropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
            }
            sendResult(result)
        }
    }

    @Suppress("RestrictedApi")
    override fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>) {
        launch(DispatcherProvider.IO) {
            Log.d(TAG, "checkBalance")
            val amount = paymentComponentState.data.amount
            val paymentMethod = paymentComponentState.data.paymentMethod
            if (paymentMethod != null && amount != null) {
                val paymentMethodJson = PaymentMethodDetails.SERIALIZER.serialize(paymentMethod)
                val amountJson = Amount.SERIALIZER.serialize(amount)

                val request = createBalanceRequest(
                    paymentMethodJson,
                    amountJson,
                    keyValueStorage.getMerchantAccount(),
                )

                val response = paymentsRepository.getBalance(request)
                val result = handleBalanceResponse(response)
                sendBalanceResult(result)
            } else {
                val result = BalanceDropInServiceResult.Error(
                    errorDialog = ErrorDialog(message = "amount or paymentMethod is null."),
                )
                sendBalanceResult(result)
            }
        }
    }

    @Suppress("SwallowedException")
    private fun handleBalanceResponse(jsonResponse: JSONObject?): BalanceDropInServiceResult {
        return if (jsonResponse != null) {
            when (val resultCode = jsonResponse.optString("resultCode")) {
                "Success" -> BalanceDropInServiceResult.Balance(BalanceResult.SERIALIZER.deserialize(jsonResponse))
                "NotEnoughBalance" -> {
                    try {
                        BalanceDropInServiceResult.Balance(BalanceResult.SERIALIZER.deserialize(jsonResponse))
                    } catch (e: ModelSerializationException) {
                        BalanceDropInServiceResult.Error(
                            errorDialog = ErrorDialog(message = "Not enough balance"),
                            dismissDropIn = false,
                        )
                    }
                }

                else -> BalanceDropInServiceResult.Error(
                    errorDialog = ErrorDialog(message = resultCode),
                    dismissDropIn = false,
                )
            }
        } else {
            Log.e(TAG, "FAILED")
            BalanceDropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
        }
    }

    @Suppress("RestrictedApi")
    override fun onOrderRequest() {
        launch(DispatcherProvider.IO) {
            Log.d(TAG, "createOrder")

            val paymentRequest = createOrderRequest(
                keyValueStorage.getAmount(),
                keyValueStorage.getMerchantAccount(),
            )

            val response = paymentsRepository.createOrder(paymentRequest)

            val result = handleOrderResponse(response)
            sendOrderResult(result)
        }
    }

    private fun handleOrderResponse(jsonResponse: JSONObject?): OrderDropInServiceResult {
        return if (jsonResponse != null) {
            when (val resultCode = jsonResponse.optString("resultCode")) {
                "Success" -> OrderDropInServiceResult.OrderCreated(OrderResponse.SERIALIZER.deserialize(jsonResponse))
                else -> OrderDropInServiceResult.Error(
                    errorDialog = ErrorDialog(message = resultCode),
                    dismissDropIn = false,
                )
            }
        } else {
            Log.e(TAG, "FAILED")
            OrderDropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
        }
    }

    @Suppress("RestrictedApi")
    override fun onOrderCancel(order: Order, shouldUpdatePaymentMethods: Boolean) {
        launch(DispatcherProvider.IO) {
            Log.d(TAG, "cancelOrder")
            val orderJson = Order.SERIALIZER.serialize(order)
            val request = createCancelOrderRequest(
                orderJson,
                keyValueStorage.getMerchantAccount(),
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
            Log.v(TAG, "cancelOrder response - ${jsonResponse.toStringPretty()}")
            when (val resultCode = jsonResponse.optString("resultCode")) {
                "Received" -> {
                    if (shouldUpdatePaymentMethods) fetchPaymentMethods()
                    null
                }

                else -> DropInServiceResult.Error(
                    errorDialog = ErrorDialog(message = resultCode),
                    dismissDropIn = false,
                )
            }
        } else {
            Log.e(TAG, "FAILED")
            DropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
        }
    }

    @Suppress("RestrictedApi")
    override fun onRemoveStoredPaymentMethod(
        storedPaymentMethod: StoredPaymentMethod,
    ) {
        launch(DispatcherProvider.IO) {
            val storedPaymentMethodId = storedPaymentMethod.id.orEmpty()
            val isSuccessfullyRemoved = paymentsRepository.removeStoredPaymentMethod(
                storedPaymentMethodId = storedPaymentMethodId,
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
            )
            val result = handleRemoveStoredPaymentMethodResult(storedPaymentMethodId, isSuccessfullyRemoved)
            sendRecurringResult(result)
        }
    }

    private fun handleRemoveStoredPaymentMethodResult(
        storedPaymentMethodId: String,
        isSuccessfullyRemoved: Boolean,
    ): RecurringDropInServiceResult {
        return if (isSuccessfullyRemoved) {
            Log.v(TAG, "removeStoredPaymentMethod response successful")
            RecurringDropInServiceResult.PaymentMethodRemoved(storedPaymentMethodId)
        } else {
            Log.e(TAG, "FAILED")
            RecurringDropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
        }
    }

    override fun onRedirect() {
        Log.d(TAG, "On redirect")
    }

    override fun onBinValue(binValue: String) {
        Log.d(TAG, "On bin value: $binValue")
    }

    override fun onBinLookup(data: List<BinLookupData>) {
        Log.d(TAG, "On bin lookup: ${data.map { it.brand }}")
    }

    override fun onAddressLookupQueryChanged(query: String) {
        Log.d(TAG, "On address lookup query: $query")
        addressLookupRepository.onQuery(query)
    }

    override fun onAddressLookupCompletion(lookupAddress: LookupAddress): Boolean {
        Log.d(TAG, "On address lookup query completion: $lookupAddress")
        addressLookupRepository.onAddressLookupCompleted(lookupAddress)
        return true
    }

    companion object {
        private val TAG = getLogTag()
        private const val RESULT_REFUSED = "refused"
    }
}
