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
import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.paymentmethod.BlikPaymentMethod
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
import com.adyen.checkout.ui.core.R as UICoreR

@HiltViewModel
class BlikViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel(), ComponentCallback<BlikComponentState> {

    private val _blikViewState = MutableStateFlow<BlikViewState>(BlikViewState.Loading)
    val blikViewState = _blikViewState.asStateFlow()

    private val _events = MutableSharedFlow<BlikEvent>()
    val events: SharedFlow<BlikEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch { _blikViewState.emit(fetchPaymentMethods()) }
    }

    private suspend fun fetchPaymentMethods(): BlikViewState = withContext(Dispatchers.IO) {
        if (keyValueStorage.getAmount().currency != CheckoutCurrency.PLN.name) {
            return@withContext BlikViewState.Error(R.string.currency_code_error, CheckoutCurrency.PLN.name)
        } else if (keyValueStorage.getCountry() != POLAND_COUNTRY_CODE) {
            return@withContext BlikViewState.Error(R.string.country_code_error, POLAND_COUNTRY_CODE)
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

        val blikPaymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { BlikComponent.PROVIDER.isPaymentMethodSupported(it) }

        if (blikPaymentMethod == null) {
            BlikViewState.Error(UICoreR.string.error_dialog_title)
        } else {
            val componentData = BlikComponentData(
                paymentMethod = blikPaymentMethod,
                callback = this@BlikViewModel,
            )
            BlikViewState.ShowComponent(componentData)
        }
    }

    override fun onSubmit(state: BlikComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
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
                    ?: error("Return url should be set"),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                shopperEmail = keyValueStorage.getShopperEmail(),
            )

            handlePaymentResponse(paymentsRepository.makePaymentsRequest(paymentRequest))
        }
    }

    private suspend fun handlePaymentResponse(json: JSONObject?) {
        json?.let {
            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    _blikViewState.value = BlikViewState.Action(action)
                }
                else -> _events.emit(BlikEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(BlikEvent.PaymentResult("Failed"))
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    companion object {
        private const val POLAND_COUNTRY_CODE = "PL"
    }
}
