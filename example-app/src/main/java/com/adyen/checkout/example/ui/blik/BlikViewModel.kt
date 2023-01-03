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
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.example.R
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

    init {
        viewModelScope.launch { _blikViewState.emit(fetchPaymentMethods()) }
    }

    private suspend fun fetchPaymentMethods(): BlikViewState = withContext(Dispatchers.IO) {
        if (keyValueStorage.getAmount().currency != CheckoutCurrency.PLN.name) {
            return@withContext BlikViewState.Error(R.string.blik_currency_error)
        } else if (keyValueStorage.getCountry() != POLAND_COUNTRY_CODE) {
            return@withContext BlikViewState.Error(R.string.blik_country_error)
        }

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
            BlikViewState.Error(R.string.error_dialog_title)
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
            is PaymentComponentEvent.Submit -> {
                makePayment(event.state.data)
            }
        }
    }

    private fun onBlikComponentState(event: PaymentComponentState<BlikPaymentMethod>) {
        blikComponentState = event
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(BlikEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }

    private fun makePayment(data: PaymentComponentData<BlikPaymentMethod>) {
        _blikViewState.value = BlikViewState.Loading
        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch {
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
                    _blikViewState.value = BlikViewState.Action(action)
                }
                else -> _events.emit(BlikEvent.PaymentResult("Success: ${json.getStringOrNull("resultCode")}"))
            }
        } ?: _events.emit(BlikEvent.PaymentResult("Failed"))
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.detailsRequestAsync(json))
        }
    }

    companion object {
        private const val POLAND_COUNTRY_CODE = "PL"
    }
}
