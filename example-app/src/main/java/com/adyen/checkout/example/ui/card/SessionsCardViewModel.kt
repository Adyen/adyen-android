/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/1/2023.
 */

package com.adyen.checkout.example.ui.card

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class SessionsCardViewModel @Inject constructor(
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

    init {
        viewModelScope.launch { launchComponent() }
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
                isThreeds2Enabled = keyValueStorage.isThreeds2Enabled(),
                redirectUrl = savedStateHandle.get<String>(SessionsCardActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                allowedPaymentMethods = listOf(paymentMethodType),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode())
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

    companion object {
        private val TAG = getLogTag()
    }
}