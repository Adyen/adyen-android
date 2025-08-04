/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/7/2025.
 */

package com.adyen.checkout.example.ui.mobilesummercard

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.data.storage.ThreeDSMode
import com.adyen.checkout.example.extensions.IODispatcher
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
internal class MSCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage
) : ViewModel(), ComponentCallback<CardComponentState> {

    lateinit var cardComponent: CardComponent

    override fun onSubmit(state: CardComponentState) {
        viewModelScope.launch(IODispatcher) {
//            val action = makePaymentCall(state.data)

            if (action != null) {
                // The action is not empty
            }
        }
    }

    private suspend fun makePaymentCall(data: PaymentComponentData<*>): Action? {
        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)
        val paymentRequest = createPaymentRequest(
            paymentComponentData = paymentComponentData,
            shopperReference = keyValueStorage.getShopperReference(),
            amount = keyValueStorage.getAmount(),
            countryCode = keyValueStorage.getCountry(),
            merchantAccount = keyValueStorage.getMerchantAccount(),
            redirectUrl = savedStateHandle.get<String>(MSCardActivity.RETURN_URL_EXTRA)
                ?: error("Return url should be set"),
            shopperEmail = keyValueStorage.getShopperEmail(),
            // 3DS2 mode
            threeDSMode = ThreeDSMode.PREFER_NATIVE,
        )

        return handlePaymentResponse(paymentsRepository.makePaymentsRequest(paymentRequest))
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
//        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    lateinit var activity: Activity
    var action: Action? = null

    private val _events = MutableSharedFlow<MSCardEvent>()
    val events: Flow<MSCardEvent> = _events

    suspend fun fetchPaymentMethods() = withContext(IODispatcher) {
        return@withContext paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )
    }

    fun getCardPaymentMethod(paymentMethodResponse: PaymentMethodsApiResponse?) = paymentMethodResponse
        ?.paymentMethods
        ?.firstOrNull { CardComponent.PROVIDER.isPaymentMethodSupported(it) }


    private suspend fun handlePaymentResponse(json: JSONObject?): Action? {
        json?.let {
            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    return action
                }

                else -> _events.emit(MSCardEvent.PaymentFinished("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(MSCardEvent.PaymentFinished("Failed"))

        return null
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(IODispatcher) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch {
            _events.emit(MSCardEvent.Error("Failed: ${error.errorMessage}"))
        }
    }
}
