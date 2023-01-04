/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/1/2023.
 */

package com.adyen.checkout.example.ui.instant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
internal class InstantViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel() {

    private val _paymentMethodFlow = MutableStateFlow<PaymentMethod?>(null)
    val paymentMethodFlow: Flow<PaymentMethod> = _paymentMethodFlow.filterNotNull()

    private val _instantViewState = MutableStateFlow<InstantViewState>(InstantViewState.Loading)
    val instantViewState: Flow<InstantViewState> = _instantViewState

    private val _events = MutableSharedFlow<InstantEvent>()
    val events: Flow<InstantEvent> = _events

    init {
        viewModelScope.launch { fetchPaymentMethods() }
    }

    private suspend fun fetchPaymentMethods() = withContext(Dispatchers.IO) {
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

        val paymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { it.type == keyValueStorage.getInstantPaymentMethodType() }

        if (paymentMethod == null) {
            _instantViewState.emit(InstantViewState.Error)
        } else {
            _paymentMethodFlow.emit(paymentMethod)
        }
    }

    fun onPaymentComponentEvent(event: PaymentComponentEvent<PaymentComponentState<PaymentMethodDetails>>) {
        when (event) {
            is PaymentComponentEvent.StateChanged -> {
                // no ops
            }
            is PaymentComponentEvent.Error -> {
                onComponentError(event.error)
            }
            is PaymentComponentEvent.ActionDetails -> {
                sendPaymentDetails(event.data)
            }
            is PaymentComponentEvent.Submit -> {
                makePayment(event.state.data)
                _instantViewState.tryEmit(InstantViewState.Loading)
            }
        }
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _instantViewState.tryEmit(InstantViewState.Loading)
        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(InstantFragment.RETURN_URL_EXTRA)
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
                    _instantViewState.tryEmit(InstantViewState.ShowComponent)
                    _events.emit(InstantEvent.AdditionalAction(action))
                }
                else -> _events.emit(InstantEvent.PaymentResult("Success: ${json.getStringOrNull("resultCode")}"))
            }
        } ?: _events.emit(InstantEvent.PaymentResult("Failed"))
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.detailsRequestAsync(json))
        }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(InstantEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }
}
