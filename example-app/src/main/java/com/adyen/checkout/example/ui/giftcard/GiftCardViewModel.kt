/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/4/2023.
 */

package com.adyen.checkout.example.ui.giftcard

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.exception.ModelSerializationException
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.IODispatcher
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createBalanceRequest
import com.adyen.checkout.example.service.createOrderRequest
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.GiftCardComponentCallback
import com.adyen.checkout.giftcard.GiftCardComponentState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
internal class GiftCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel(), GiftCardComponentCallback {

    private val _giftCardComponentDataFlow = MutableStateFlow<GiftCardComponentData?>(null)
    val giftCardComponentDataFlow: Flow<GiftCardComponentData> = _giftCardComponentDataFlow.filterNotNull()

    private val _giftCardViewStateFlow = MutableStateFlow<GiftCardViewState>(GiftCardViewState.Loading)
    internal val giftCardViewStateFlow: Flow<GiftCardViewState> = _giftCardViewStateFlow

    private val _events = MutableSharedFlow<GiftCardEvent>()
    internal val events: Flow<GiftCardEvent> = _events

    private var order: OrderRequest? = null

    init {
        viewModelScope.launch { fetchPaymentMethods() }
    }

    private suspend fun fetchPaymentMethods() = withContext(IODispatcher) {
        val paymentMethodResponse = paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )

        val giftCardPaymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { GiftCardComponent.PROVIDER.isPaymentMethodSupported(it) }

        if (giftCardPaymentMethod == null) {
            _giftCardViewStateFlow.emit(GiftCardViewState.Error)
        } else {
            _giftCardComponentDataFlow.emit(
                GiftCardComponentData(
                    paymentMethod = giftCardPaymentMethod,
                    callback = this@GiftCardViewModel,
                ),
            )
            _giftCardViewStateFlow.emit(GiftCardViewState.ShowComponent)
        }
    }

    override fun onSubmit(state: GiftCardComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    // no ops
    override fun onStateChanged(state: GiftCardComponentState) = Unit

    override fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>) {
        viewModelScope.launch(IODispatcher) {
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
                handleBalanceResponse(response)
            } else {
                _giftCardViewStateFlow.emit(GiftCardViewState.Error)
            }
        }
    }

    @Suppress("SwallowedException")
    private fun handleBalanceResponse(jsonResponse: JSONObject?) {
        if (jsonResponse != null) {
            when (jsonResponse.optString("resultCode")) {
                "Success" -> {
                    viewModelScope.launch {
                        _events.emit(
                            GiftCardEvent.Balance(
                                BalanceResult.SERIALIZER.deserialize(
                                    jsonResponse,
                                ),
                            ),
                        )
                    }
                }

                "NotEnoughBalance" -> {
                    try {
                        viewModelScope.launch {
                            _events.emit(
                                GiftCardEvent.Balance(
                                    BalanceResult.SERIALIZER.deserialize(
                                        jsonResponse,
                                    ),
                                ),
                            )
                        }
                    } catch (e: ModelSerializationException) {
                        viewModelScope.launch { _giftCardViewStateFlow.emit(GiftCardViewState.Error) }
                    }
                }

                else -> viewModelScope.launch { _giftCardViewStateFlow.emit(GiftCardViewState.Error) }
            }
        } else {
            Log.e(TAG, "FAILED")
        }
    }

    override fun onRequestOrder() {
        viewModelScope.launch(IODispatcher) {
            Log.d(TAG, "createOrder")

            val paymentRequest = createOrderRequest(
                keyValueStorage.getAmount(),
                keyValueStorage.getMerchantAccount(),
            )

            val response = paymentsRepository.createOrder(paymentRequest)

            handleOrderResponse(response)
        }
    }

    private fun handleOrderResponse(jsonResponse: JSONObject?) {
        if (jsonResponse != null) {
            when (jsonResponse.optString("resultCode")) {
                "Success" -> viewModelScope.launch {
                    val orderResponse = OrderResponse.SERIALIZER.deserialize(jsonResponse)
                    _events.emit(GiftCardEvent.OrderCreated(orderResponse))
                    order = OrderRequest(
                        orderData = orderResponse.orderData,
                        pspReference = orderResponse.pspReference,
                    )
                }

                else -> viewModelScope.launch { _giftCardViewStateFlow.emit(GiftCardViewState.Error) }
            }
        } else {
            Log.e(TAG, "FAILED")
            viewModelScope.launch { _giftCardViewStateFlow.emit(GiftCardViewState.Error) }
        }
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _giftCardViewStateFlow.value = GiftCardViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(IODispatcher) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(GiftCardActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                shopperEmail = keyValueStorage.getShopperEmail(),
            )

            handlePaymentResponse(paymentsRepository.makePaymentsRequest(paymentRequest))
        }
    }

    private suspend fun handlePaymentResponse(json: JSONObject?) {
        viewModelScope.launch { _giftCardViewStateFlow.emit(GiftCardViewState.ShowComponent) }
        json?.let {
            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    handleAction(action)
                }

                isRefusedInPartialPaymentFlow(json) -> {
                    _events.emit(GiftCardEvent.PaymentResult("Refused in Partial Payment Flow"))
                }

                isNonFullyPaidOrder(json) -> {
                    order = getOrderFromResponse(json).let {
                        Order(
                            pspReference = it.pspReference,
                            orderData = it.orderData,
                        )
                    }
                }

                else -> _events.emit(GiftCardEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(GiftCardEvent.PaymentResult("Failed"))
    }

    private fun isRefusedInPartialPaymentFlow(jsonResponse: JSONObject) =
        isRefused(jsonResponse) && isNonFullyPaidOrder(jsonResponse)

    private fun isRefused(jsonResponse: JSONObject): Boolean {
        return jsonResponse.optString("resultCode")
            .equals(other = RESULT_REFUSED, ignoreCase = true)
    }

    private fun isNonFullyPaidOrder(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("order") && (getOrderFromResponse(jsonResponse).remainingAmount?.value ?: 0) > 0
    }

    private fun getOrderFromResponse(jsonResponse: JSONObject): OrderResponse {
        val orderJSON = jsonResponse.getJSONObject("order")
        return OrderResponse.SERIALIZER.deserialize(orderJSON)
    }

    private fun handleAction(action: Action) {
        viewModelScope.launch { _events.emit(GiftCardEvent.AdditionalAction(action)) }
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(IODispatcher) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(GiftCardEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }

    fun reloadComponentWithOrder() {
        val order = this.order
        val giftCardComponentData = _giftCardComponentDataFlow.value
        if (order != null && giftCardComponentData != null) {
            viewModelScope.launch {
                _events.emit(
                    GiftCardEvent.ReloadComponent(
                        order,
                        giftCardComponentData,
                    ),
                )
            }
        }
    }

    companion object {
        private val TAG = getLogTag()
        private const val RESULT_REFUSED = "refused"
    }
}
