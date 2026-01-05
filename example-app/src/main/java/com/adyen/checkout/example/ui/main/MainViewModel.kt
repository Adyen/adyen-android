/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.dropin.old.DropInResult
import com.adyen.checkout.dropin.old.SessionDropInResult
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.IntegrationFlow
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.configuration.ConfigurationProvider
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.adyen.checkout.components.core.CheckoutConfiguration as OldCheckoutConfiguration

@Suppress("TooManyFunctions")
@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val checkoutConfigurationProvider: ConfigurationProvider,
) : ViewModel() {

    private val lifecycleResumed: MutableSharedFlow<Unit> = MutableSharedFlow()
    private val useSessions: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val showLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _mainViewState: MutableStateFlow<MainViewState> = MutableStateFlow(getInitialViewState())
    val mainViewState: Flow<MainViewState> = _mainViewState

    private val _eventFlow: MutableSharedFlow<MainEvent> = MutableSharedFlow(extraBufferCapacity = 1)
    val eventFlow: Flow<MainEvent> = _eventFlow

    init {
        viewModelScope.launch {
            refreshUseSessions()
            combineViewStateFlows()
        }
    }

    private suspend fun combineViewStateFlows() {
        combine(
            lifecycleResumed,
            useSessions,
            showLoading,
        ) { _, useSessions, showLoading ->
            getViewState(useSessions, showLoading)
        }.collect {
            loadViewState(it)
        }
    }

    internal fun onResume() {
        viewModelScope.launch {
            lifecycleResumed.emit(Unit)
            refreshUseSessions()
        }
    }

    private suspend fun refreshUseSessions() {
        useSessions.emit(keyValueStorage.getIntegrationFlow() == IntegrationFlow.SESSIONS)
    }

    @Suppress("CyclomaticComplexMethod")
    fun onComponentEntryClick(entry: ComponentItem.Entry) {
        when (entry) {
            is ComponentItem.Entry.Bacs -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Bacs))
            is ComponentItem.Entry.Blik -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Blik))
            is ComponentItem.Entry.Card -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Card))
            is ComponentItem.Entry.Klarna -> _eventFlow.tryEmit(
                MainEvent.NavigateTo(MainNavigation.Instant(PAYMENT_METHOD_KLARNA)),
            )

            is ComponentItem.Entry.PayPal ->
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Instant(PAYMENT_METHOD_PAYPAL)))

            is ComponentItem.Entry.Instant ->
                _eventFlow.tryEmit(
                    MainEvent.NavigateTo(MainNavigation.Instant(keyValueStorage.getInstantPaymentMethodType())),
                )

            is ComponentItem.Entry.CardWithSession ->
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.CardWithSession))

            is ComponentItem.Entry.CardWithSessionTakenOver ->
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.CardWithSessionTakenOver))

            is ComponentItem.Entry.GiftCard -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.GiftCard))
            is ComponentItem.Entry.GiftCardWithSession ->
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.GiftCardWithSession))

            is ComponentItem.Entry.GooglePay -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.GooglePay))
            is ComponentItem.Entry.GooglePayWithSession -> {
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.GooglePayWithSession))
            }

            is ComponentItem.Entry.V6 -> {
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.V6))
            }

            is ComponentItem.Entry.V6Sessions -> {
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.V6Sessions))
            }

            is ComponentItem.Entry.DropIn -> startDropInFlow()
            is ComponentItem.Entry.DropInWithSession -> startSessionDropInFlow(false)
            is ComponentItem.Entry.DropInWithCustomSession -> startSessionDropInFlow(true)
            is ComponentItem.Entry.V6DropIn -> startV6DropInFlow()
            is ComponentItem.Entry.V6DropInWithSession -> startV6SessionsDropInFlow()
        }
    }

    private fun startDropInFlow() {
        viewModelScope.launch {
            showLoading(true)

            val paymentMethods = paymentsRepository.getPaymentMethodsOld(createPaymentMethodRequest())

            showLoading(false)

            if (paymentMethods != null) {
                val checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.DropIn(paymentMethods, checkoutConfiguration)))
            } else {
                onError("Something went wrong while fetching payment methods")
            }
        }
    }

    private fun startSessionDropInFlow(takeOverSession: Boolean) {
        viewModelScope.launch {
            showLoading(true)

            val checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig

            val session = getSessionOld(checkoutConfiguration)

            showLoading(false)

            if (session != null) {
                val navigation = if (takeOverSession) {
                    MainNavigation.DropInWithCustomSession(session, checkoutConfiguration)
                } else {
                    MainNavigation.DropInWithSession(session, checkoutConfiguration)
                }
                _eventFlow.tryEmit(MainEvent.NavigateTo(navigation))
            } else {
                onError("Something went wrong while starting session")
            }
        }
    }

    private fun startV6DropInFlow() {
        viewModelScope.launch {
            showLoading(true)

            val paymentMethods = paymentsRepository.getPaymentMethods(createPaymentMethodRequest())

            // TODO - Get config from provider
            @Suppress("MagicNumber")
            val checkoutConfiguration = CheckoutConfiguration(
                environment = Environment.TEST,
                clientKey = BuildConfig.CLIENT_KEY,
                amount = Amount("USD", 1337),
            )

            val result = paymentMethods?.let {
                Checkout.setup(
                    paymentMethods,
                    checkoutConfiguration,
                )
            }

            showLoading(false)

            if (result is Checkout.Result.Success) {
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.V6DropIn(result.checkoutContext)))
            } else {
                onError("Something went wrong while fetching payment methods")
            }
        }
    }

    private fun startV6SessionsDropInFlow() {
        viewModelScope.launch {
            showLoading(true)

            // TODO - Get config from provider
            @Suppress("MagicNumber")
            val checkoutConfiguration = CheckoutConfiguration(
                environment = Environment.TEST,
                clientKey = BuildConfig.CLIENT_KEY,
                amount = Amount("USD", 1337),
            )

            val sessionModel = paymentsRepository.createSession(createSessionRequest())

            val result = sessionModel?.let {
                Checkout.setup(
                    sessionModel,
                    checkoutConfiguration,
                )
            }

            showLoading(false)

            if (result is Checkout.Result.Success) {
                val navigation = MainNavigation.V6DropInWithSession(result.checkoutContext)
                _eventFlow.tryEmit(MainEvent.NavigateTo(navigation))
            } else {
                onError("Something went wrong while starting session")
            }
        }
    }

    private fun createPaymentMethodRequest() = getPaymentMethodRequest(
        merchantAccount = keyValueStorage.getMerchantAccount(),
        shopperReference = keyValueStorage.getShopperReference(),
        amount = keyValueStorage.getAmount(),
        countryCode = keyValueStorage.getCountry(),
        shopperLocale = keyValueStorage.getShopperLocale(),
        splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
    )

    private fun createSessionRequest() = getSessionRequest(
        merchantAccount = keyValueStorage.getMerchantAccount(),
        shopperReference = keyValueStorage.getShopperReference(),
        amount = keyValueStorage.getAmount(),
        countryCode = keyValueStorage.getCountry(),
        shopperLocale = keyValueStorage.getShopperLocale(),
        splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
        threeDSMode = keyValueStorage.getThreeDSMode(),
        redirectUrl = savedStateHandle.get<String>(MainActivity.RETURN_URL_EXTRA)
            ?: error("Return url should be set"),
        shopperEmail = keyValueStorage.getShopperEmail(),
        installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
        showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
        showRemovePaymentMethodButton = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
    )

    private suspend fun getSessionOld(checkoutConfiguration: OldCheckoutConfiguration): CheckoutSession? {
        val sessionModel = paymentsRepository.createSessionOld(createSessionRequest()) ?: return null

        return getCheckoutSession(sessionModel, checkoutConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        checkoutConfiguration: OldCheckoutConfiguration,
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> {
                onError("Something went wrong while starting session")
                null
            }
        }
    }

    private fun onError(message: String) {
        _eventFlow.tryEmit(MainEvent.Toast(message))
    }

    fun onSessionsToggled(enable: Boolean) {
        viewModelScope.launch {
            val integrationFlow = if (enable) {
                IntegrationFlow.SESSIONS
            } else {
                IntegrationFlow.ADVANCED
            }
            keyValueStorage.setIntegrationFlow(integrationFlow)
            useSessions.emit(enable)
        }
    }

    private suspend fun showLoading(loading: Boolean) {
        showLoading.emit(loading)
    }

    private fun getInitialViewState(): MainViewState {
        val useSessions = useSessions.value
        val showLoading = showLoading.value
        return getViewState(useSessions, showLoading)
    }

    private fun getViewState(
        useSessions: Boolean,
        showLoading: Boolean,
    ): MainViewState {
        return MainViewState(
            listItems = getListItems(useSessions),
            useSessions = useSessions,
            showLoading = showLoading,
        )
    }

    private suspend fun loadViewState(mainViewState: MainViewState) {
        _mainViewState.emit(mainViewState)
    }

    private fun getListItems(useSessions: Boolean): List<ComponentItem> {
        return if (useSessions) {
            ComponentItemProvider.getSessionItems()
        } else {
            val instantPaymentMethodType = keyValueStorage.getInstantPaymentMethodType()
            ComponentItemProvider.getDefaultItems(instantPaymentMethodType)
        }
    }

    fun onDropInResult(dropInResult: DropInResult?) {
        val message = when (dropInResult) {
            is DropInResult.CancelledByUser -> "Canceled by user"
            is DropInResult.Error -> dropInResult.reason ?: "DropInResult is error without reason"
            is DropInResult.Finished -> dropInResult.result
            null -> "DropInResult is null"
        }
        _eventFlow.tryEmit(MainEvent.Toast(message))
    }

    fun onDropInResult(sessionDropInResult: SessionDropInResult?) {
        val message = when (sessionDropInResult) {
            is SessionDropInResult.CancelledByUser -> "Canceled by user"
            is SessionDropInResult.Error -> sessionDropInResult.reason ?: "DropInResult is error without reason"
            is SessionDropInResult.Finished -> sessionDropInResult.result.resultCode ?: "Result code is null"
            null -> "DropInResult is null"
        }
        _eventFlow.tryEmit(MainEvent.Toast(message))
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
    }

    companion object {
        private val TAG = getLogTag()
        private const val PAYMENT_METHOD_PAYPAL = "paypal"
        private const val PAYMENT_METHOD_KLARNA = "klarna"
    }
}
