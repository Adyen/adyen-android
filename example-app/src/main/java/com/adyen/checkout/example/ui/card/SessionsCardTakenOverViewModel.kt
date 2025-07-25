/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/1/2023.
 */

package com.adyen.checkout.example.ui.card

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardType
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.IODispatcher
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.AddressLookupCompletionResult
import com.adyen.checkout.example.repositories.AddressLookupRepository
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class SessionsCardTakenOverViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val addressLookupRepository: AddressLookupRepository,
    checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel(), SessionComponentCallback<CardComponentState> {

    private val _sessionsCardComponentDataFlow = MutableStateFlow<SessionsCardComponentData?>(null)
    val sessionsCardComponentDataFlow: Flow<SessionsCardComponentData> = _sessionsCardComponentDataFlow.filterNotNull()

    private val _cardViewState = MutableStateFlow<CardViewState>(CardViewState.Loading)
    val cardViewState: Flow<CardViewState> = _cardViewState

    private val _events = MutableSharedFlow<CardEvent>()
    val events: Flow<CardEvent> = _events

    private val checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig

    private var isFlowTakenOver: Boolean
        get() = savedStateHandle[IS_SESSIONS_FLOW_TAKEN_OVER_KEY] ?: false
        set(isFlowTakenOver) {
            savedStateHandle[IS_SESSIONS_FLOW_TAKEN_OVER_KEY] = isFlowTakenOver
        }

    init {
        viewModelScope.launch { launchComponent() }
        addressLookupRepository.addressLookupOptionsFlow
            .onEach { options ->
                _events.emit(CardEvent.AddressLookup(options))
            }.launchIn(viewModelScope)
    }

    private suspend fun launchComponent() {
        val paymentMethodType = PaymentMethodTypes.SCHEME
        val checkoutSession = getSession(paymentMethodType)
        if (checkoutSession == null) {
            Log.e(TAG, "Failed to fetch session")
            _cardViewState.emit(CardViewState.Error)
            return
        }
        val paymentMethod = checkoutSession.getPaymentMethod(paymentMethodType)
        if (paymentMethod == null) {
            Log.e(TAG, "Session does not contain SCHEME payment method")
            _cardViewState.emit(CardViewState.Error)
            return
        }

        _sessionsCardComponentDataFlow.emit(
            SessionsCardComponentData(
                checkoutSession = checkoutSession,
                paymentMethod = paymentMethod,
                callback = this,
            ),
        )
        _cardViewState.emit(CardViewState.ShowComponent)
    }

    private suspend fun getSession(paymentMethodType: String): CheckoutSession? {
        val sessionModel = paymentsRepository.createSessionOld(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                redirectUrl = savedStateHandle.get<String>(SessionsCardTakenOverActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                allowedPaymentMethods = listOf(paymentMethodType),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
                showRemovePaymentMethodButton = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
            ),
        ) ?: return null

        return getCheckoutSession(sessionModel, checkoutConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> null
        }
    }

    override fun onAction(action: Action) {
        viewModelScope.launch { _events.emit(CardEvent.AdditionalAction(action)) }
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    override fun onFinished(result: SessionPaymentResult) {
        viewModelScope.launch { _events.emit(CardEvent.PaymentResult(result.resultCode.orEmpty())) }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(CardEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }

    override fun onSubmit(state: CardComponentState): Boolean {
        // for this example we will only take over the flow if the brand is MASTERCARD
        if (state.cardBrand != CardBrand(CardType.MASTERCARD)) return false

        isFlowTakenOver = true
        makePayment(state.data)
        return true
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData): Boolean {
        if (isFlowTakenOver) sendPaymentDetails(actionComponentData)
        return isFlowTakenOver
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _cardViewState.value = CardViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(IODispatcher) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(SessionsCardTakenOverActivity.RETURN_URL_EXTRA)
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
                    handleAction(action)
                }

                else -> _events.emit(CardEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(CardEvent.PaymentResult("Failed"))
    }

    private suspend fun handleAction(action: Action) {
        _events.emit(CardEvent.AdditionalAction(action))
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(IODispatcher) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    override fun onLoading(isLoading: Boolean) {
        val state = if (isLoading) {
            Log.d(TAG, "Show loading")
            CardViewState.Loading
        } else {
            Log.d(TAG, "Don't show loading")
            CardViewState.ShowComponent
        }
        _cardViewState.tryEmit(state)
    }

    fun onAddressLookupQueryChanged(query: String) {
        addressLookupRepository.onQuery(query)
    }

    fun onAddressLookupCompletion(lookupAddress: LookupAddress) {
        viewModelScope.launch {
            val event = when (val lookupResult = addressLookupRepository.onAddressLookupCompleted(lookupAddress)) {
                is AddressLookupCompletionResult.Address -> CardEvent.AddressLookupCompleted(lookupResult.lookupAddress)
                is AddressLookupCompletionResult.Error -> CardEvent.AddressLookupError(lookupResult.message)
            }
            _events.emit(event)
        }
    }

    companion object {
        private val TAG = getLogTag()
        private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
    }
}
