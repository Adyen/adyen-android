/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.googlepay.GooglePayComponentState
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
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
@HiltViewModel
internal class GooglePayViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel(),
    ComponentCallback<GooglePayComponentState> {

    private val checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig

    private val _googleComponentDataFlow = MutableStateFlow<GooglePayComponentData?>(null)
    val googleComponentDataFlow: Flow<GooglePayComponentData> = _googleComponentDataFlow.filterNotNull()

    private val _viewState = MutableStateFlow<GooglePayViewState>(GooglePayViewState.Loading)
    val viewState: Flow<GooglePayViewState> = _viewState

    private val _events = MutableSharedFlow<GooglePayEvent>()
    val events: Flow<GooglePayEvent> = _events

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
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )

        val paymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { GooglePayComponent.PROVIDER.isPaymentMethodSupported(it) }

        if (paymentMethod == null) {
            _viewState.emit(GooglePayViewState.Error(UICoreR.string.error_dialog_title))
            return@withContext
        }

        _googleComponentDataFlow.emit(
            GooglePayComponentData(
                paymentMethod,
                checkoutConfiguration,
                this@GooglePayViewModel,
            ),
        )
    }

    override fun onAvailabilityResult(isAvailable: Boolean) {
        viewModelScope.launch {
            if (isAvailable) {
                _viewState.emit(GooglePayViewState.ShowButton)
            } else {
                _viewState.emit(GooglePayViewState.Error(R.string.google_pay_unavailable_error))
            }
        }
    }

    override fun onSubmit(state: GooglePayComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(GooglePayEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    private suspend fun handlePaymentResponse(json: JSONObject?) {
        json?.let {
            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    handleAction(action)
                }

                else -> _events.emit(GooglePayEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(GooglePayEvent.PaymentResult("Failed"))
    }

    private suspend fun handleAction(action: Action) {
        _viewState.emit(GooglePayViewState.ShowComponent)
        _events.emit(GooglePayEvent.AdditionalAction(action))
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _viewState.value = GooglePayViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(GooglePayFragment.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                shopperEmail = keyValueStorage.getShopperEmail(),
            )

            handlePaymentResponse(paymentsRepository.makePaymentsRequest(paymentRequest))
        }
    }
}
