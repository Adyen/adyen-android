/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/1/2023.
 */

package com.adyen.checkout.example.ui.bacs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.bacs.BacsDirectDebitComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.R
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
import java.util.Locale
import javax.inject.Inject
import com.adyen.checkout.ui.core.R as UICoreR

@HiltViewModel
internal class BacsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel(), ComponentCallback<BacsDirectDebitComponentState> {

    private val _bacsComponentDataFlow = MutableStateFlow<BacsComponentData?>(null)
    val bacsComponentDataFlow: Flow<BacsComponentData> = _bacsComponentDataFlow.filterNotNull()

    private val _viewState = MutableStateFlow<BacsViewState>(BacsViewState.Loading)
    val viewState: Flow<BacsViewState> = _viewState

    private val _events = MutableSharedFlow<BacsEvent>()
    val events: Flow<BacsEvent> = _events

    init {
        viewModelScope.launch { fetchPaymentMethods() }
    }

    private suspend fun fetchPaymentMethods() = withContext(Dispatchers.IO) {
        val validationError = if (keyValueStorage.getAmount().currency != CheckoutCurrency.GBP.name) {
            BacsViewState.Error(R.string.currency_code_error, CheckoutCurrency.GBP.name)
        } else if (keyValueStorage.getCountry() != Locale.UK.country) {
            BacsViewState.Error(R.string.country_code_error, Locale.UK.country)
        } else {
            null
        }

        validationError?.let {
            _viewState.emit(it)
            return@withContext
        }

        val paymentMethodResponse = paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            )
        )

        val paymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(it) }

        if (paymentMethod == null) {
            _viewState.emit(BacsViewState.Error(UICoreR.string.error_dialog_title))
        } else {
            _bacsComponentDataFlow.emit(
                BacsComponentData(
                    paymentMethod,
                    this@BacsViewModel
                )
            )
            _viewState.emit(BacsViewState.ShowComponent)
        }
    }

    override fun onSubmit(state: BacsDirectDebitComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(BacsEvent.PaymentResult("Failed: ${error.errorMessage}")) }
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

                else -> _events.emit(BacsEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(BacsEvent.PaymentResult("Failed"))
    }

    private suspend fun handleAction(action: Action) {
        _viewState.emit(BacsViewState.ShowComponent)
        _events.emit(BacsEvent.AdditionalAction(action))
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _viewState.value = BacsViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(BacsFragment.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                shopperEmail = keyValueStorage.getShopperEmail(),
            )

            handlePaymentResponse(paymentsRepository.makePaymentsRequest(paymentRequest))
        }
    }
}
