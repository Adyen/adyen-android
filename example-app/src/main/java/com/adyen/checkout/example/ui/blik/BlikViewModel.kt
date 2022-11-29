/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/11/2022.
 */

package com.adyen.checkout.example.ui.blik

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class BlikViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel() {

    private val _blikViewState = MutableStateFlow<BlikViewState>(BlikViewState.Loading)
    val blikViewState = _blikViewState.asStateFlow()

    private val _events = MutableSharedFlow<BlikEvent>()
    val events: SharedFlow<BlikEvent> = _events.asSharedFlow()

    private var blikComponentState: PaymentComponentState<BlikPaymentMethod>? = null

    fun onCreate() {
        viewModelScope.launch { _blikViewState.emit(fetchPaymentMethods()) }
    }

    private suspend fun fetchPaymentMethods(): BlikViewState = withContext(Dispatchers.IO) {
        val paymentMethodResponse = paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources()
            )
        )

        val blikPaymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { BlikComponent.PROVIDER.isPaymentMethodSupported(it) }

        if (blikPaymentMethod == null) {
            BlikViewState.Error
        } else {
            BlikViewState.ShowComponent(blikPaymentMethod)
        }
    }

    fun onPaymentComponentEvent(event: PaymentComponentEvent<PaymentComponentState<BlikPaymentMethod>>) {
        when (event) {
            is PaymentComponentEvent.StateChanged -> {
                onBlikComponentState(event.state)
            }
            is PaymentComponentEvent.Error -> {
                onComponentError(event.error)
            }
            is PaymentComponentEvent.ActionDetails -> {
                sendPaymentDetails(event.data)
            }
        }
    }

    private fun onBlikComponentState(event: PaymentComponentState<BlikPaymentMethod>) {
        blikComponentState = event
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(BlikEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }

    fun onPayClick() {
        blikComponentState?.let {
            if (it.isValid) {
                makePayment(it.data)
            } else {
                viewModelScope.launch { _events.emit(BlikEvent.Invalid) }
            }
        } ?: run {
            _blikViewState.tryEmit(BlikViewState.Error)
        }
    }

    private fun makePayment(data: PaymentComponentData<BlikPaymentMethod>) {
        _blikViewState.value = BlikViewState.Loading
        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(BlikActivity.RETURN_URL_EXTRA)
                    ?: throw IllegalStateException("Return url should be set"),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD()
            )

            handlePaymentResponse(paymentsRepository.paymentsRequestAsync(paymentRequest))
        }
    }

    private suspend fun handlePaymentResponse(json: JSONObject?) {
        json?.let {
            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    handleAction(action)
                }
                else -> _events.emit(BlikEvent.PaymentResult("Success: ${json.getStringOrNull("resultCode")}"))
            }
        } ?: _events.emit(BlikEvent.PaymentResult("Failed"))
    }

    private suspend fun handleAction(action: Action) {
        val blikAction = when (action.type) {
            "await" -> BlikAction.Await(action)
            else -> BlikAction.Unsupported
        }

        _events.emit(BlikEvent.AdditionalAction(blikAction))
    }

    fun onActionComponentEvent(event: ActionComponentEvent) {
        when (event) {
            is ActionComponentEvent.ActionDetails -> sendPaymentDetails(event.data)
            is ActionComponentEvent.Error -> onComponentError(event.error)
        }
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.detailsRequestAsync(json))
        }
    }
}
