/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/1/2023.
 */

package com.adyen.checkout.example.ui.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.SessionComponentCallback
import com.adyen.checkout.sessions.model.SessionModel
import com.adyen.checkout.sessions.model.SessionPaymentResult
import com.adyen.checkout.sessions.provider.CheckoutSessionProvider
import com.adyen.checkout.sessions.provider.CheckoutSessionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class SessionsCardTakenOverViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel(), SessionComponentCallback<CardComponentState> {

    private val _sessionsCardComponentDataFlow = MutableStateFlow<SessionsCardComponentData?>(null)
    val sessionsCardComponentDataFlow: Flow<SessionsCardComponentData> = _sessionsCardComponentDataFlow.filterNotNull()

    private val _cardViewState = MutableStateFlow<CardViewState>(CardViewState.Loading)
    val cardViewState: Flow<CardViewState> = _cardViewState

    private val _events = MutableSharedFlow<CardEvent>()
    val events: Flow<CardEvent> = _events

    private val cardConfiguration = checkoutConfigurationProvider.getCardConfiguration()

    private var isFlowTakenOver: Boolean
        get() = savedStateHandle[IS_SESSIONS_FLOW_TAKEN_OVER_KEY] ?: false
        set(isFlowTakenOver) {
            savedStateHandle[IS_SESSIONS_FLOW_TAKEN_OVER_KEY] = isFlowTakenOver
        }

    init {
        viewModelScope.launch { launchComponent() }
    }

    private suspend fun launchComponent() {
        val paymentMethodType = PaymentMethodTypes.SCHEME
        val checkoutSession = getSession(paymentMethodType)
        if (checkoutSession == null) {
            Logger.e(TAG, "Failed to fetch session")
            _cardViewState.emit(CardViewState.Error)
            return
        }
        val paymentMethod = checkoutSession.getPaymentMethod(paymentMethodType)
        if (paymentMethod == null) {
            Logger.e(TAG, "Session does not contain SCHEME payment method")
            _cardViewState.emit(CardViewState.Error)
            return
        }

        _sessionsCardComponentDataFlow.emit(
            SessionsCardComponentData(
                checkoutSession = checkoutSession,
                paymentMethod = paymentMethod,
                callback = this
            )
        )
        _cardViewState.emit(CardViewState.ShowComponent)
    }

    private suspend fun getSession(paymentMethodType: String): CheckoutSession? {
        val sessionModel = paymentsRepository.createSession(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
                redirectUrl = savedStateHandle.get<String>(SessionsCardTakenOverActivity.RETURN_URL_EXTRA)
                    ?: throw IllegalStateException("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                allowedPaymentMethods = listOf(paymentMethodType),
            )
        ) ?: return null

        return getCheckoutSession(sessionModel, cardConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        cardConfiguration: CardConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, cardConfiguration)) {
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
        if (state.cardType != CardType(CardBrand.MASTERCARD)) return false

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

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(SessionsCardTakenOverActivity.RETURN_URL_EXTRA)
                    ?: throw IllegalStateException("Return url should be set"),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD()
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
                else -> _events.emit(CardEvent.PaymentResult("Success: ${json.getStringOrNull("resultCode")}"))
            }
        } ?: _events.emit(CardEvent.PaymentResult("Failed"))
    }

    private suspend fun handleAction(action: Action) {
        _events.emit(CardEvent.AdditionalAction(action))
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    override fun onLoading(isLoading: Boolean) {
        val state = if (isLoading) {
            Logger.d(TAG, "Show loading")
            CardViewState.Loading
        } else {
            Logger.d(TAG, "Don't show loading")
            CardViewState.ShowComponent
        }
        _cardViewState.tryEmit(state)
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
    }
}
