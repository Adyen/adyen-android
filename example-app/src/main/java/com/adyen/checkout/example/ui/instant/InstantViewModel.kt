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
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.PermissionHandlerCallback
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.IODispatcher
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.instant.InstantComponentState
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
internal class InstantViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel(), ComponentCallback<InstantComponentState> {

    private val _instantComponentDataFlow = MutableStateFlow<InstantComponentData?>(null)
    val instantComponentDataFlow: Flow<InstantComponentData> = _instantComponentDataFlow.filterNotNull()

    private val _instantViewState = MutableStateFlow<InstantViewState>(InstantViewState.Loading)
    val instantViewState: Flow<InstantViewState> = _instantViewState

    private val _events = MutableSharedFlow<InstantEvent>()
    val events: Flow<InstantEvent> = _events

    private var permissionCallback: PermissionHandlerCallback? = null

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

        val paymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { it.type == savedStateHandle.get<String>(InstantFragment.PAYMENT_METHOD_TYPE_EXTRA) }

        if (paymentMethod == null) {
            _instantViewState.emit(
                InstantViewState.Error(
                    "Payment method unavailable, make sure you set the correct country code and currency.",
                ),
            )
        } else {
            _instantComponentDataFlow.emit(
                InstantComponentData(
                    paymentMethod = paymentMethod,
                    callback = this@InstantViewModel,
                ),
            )
        }
    }

    override fun onSubmit(state: InstantComponentState) {
        makePayment(state.data)
        _instantViewState.tryEmit(InstantViewState.Loading)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    override fun onPermissionRequest(requiredPermission: String, permissionCallback: PermissionHandlerCallback) {
        this.permissionCallback = permissionCallback

        viewModelScope.launch {
            _events.emit(InstantEvent.PermissionRequest(requiredPermission))
        }
    }

    fun onPermissionResult(requestedPermission: String, isGranted: Boolean) {
        if (isGranted) {
            permissionCallback?.onPermissionGranted(requestedPermission)
        } else {
            permissionCallback?.onPermissionDenied(requestedPermission)
        }

        permissionCallback = null
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _instantViewState.tryEmit(InstantViewState.Loading)
        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(IODispatcher) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                returnUrl = savedStateHandle.get<String>(InstantFragment.RETURN_URL_EXTRA)
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
                    _instantViewState.tryEmit(InstantViewState.ShowComponent)
                    _events.emit(InstantEvent.AdditionalAction(action))
                }

                else -> _events.emit(InstantEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(InstantEvent.PaymentResult("Failed"))
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(IODispatcher) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(InstantEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }
}
