/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.SessionDropInResult
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel() {

    private val _viewState = MutableStateFlow<MainViewState>(getComponentItemsViewState())
    val viewState: Flow<MainViewState> = _viewState

    private val _eventFlow = MutableSharedFlow<MainEvent>(extraBufferCapacity = 1)
    val eventFlow: Flow<MainEvent> = _eventFlow

    fun onComponentEntryClick(entry: ComponentItem.Entry) {
        when (entry) {
            ComponentItem.Entry.Bacs -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Bacs))
            ComponentItem.Entry.Blik -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Blik))
            ComponentItem.Entry.Card -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Card))
            ComponentItem.Entry.Instant -> _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.Instant))
            ComponentItem.Entry.CardWithSession ->
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.CardWithSession))
            ComponentItem.Entry.CardWithSessionTakenOver ->
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.CardWithSessionTakenOver))
            ComponentItem.Entry.DropIn -> startDropInFlow()
            ComponentItem.Entry.DropInWithSession -> startSessionDropInFlow()
            ComponentItem.Entry.DropInWithCustomSession -> startCustomSessionDropInFlow()
        }
    }

    private fun startDropInFlow() {
        viewModelScope.launch {
            _viewState.emit(MainViewState.Loading)

            val paymentMethods = getPaymentMethods()
            if (paymentMethods != null) {
                _viewState.emit(MainViewState.Result(ComponentItemProvider.getComponentItems()))
                val dropInConfiguration = checkoutConfigurationProvider.getDropInConfiguration()
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.DropIn(paymentMethods, dropInConfiguration)))
            } else {
                onError("Something went wrong while fetching payment methods")
            }
        }
    }

    private fun startSessionDropInFlow() {
        viewModelScope.launch {
            _viewState.emit(MainViewState.Loading)

            val dropInConfiguration = checkoutConfigurationProvider.getDropInConfiguration()

            val session = getSession(dropInConfiguration)

            if (session != null) {
                _viewState.emit(MainViewState.Result(ComponentItemProvider.getComponentItems()))
                _eventFlow.tryEmit(MainEvent.NavigateTo(MainNavigation.DropInWithSession(session, dropInConfiguration)))
            } else {
                onError("Something went wrong while starting session")
            }
        }
    }

    private fun startCustomSessionDropInFlow() {
        viewModelScope.launch {
            _viewState.emit(MainViewState.Loading)

            val dropInConfiguration = checkoutConfigurationProvider.getDropInConfiguration()

            val session = getSession(dropInConfiguration)

            if (session != null) {
                _viewState.emit(MainViewState.Result(ComponentItemProvider.getComponentItems()))
                _eventFlow.tryEmit(
                    MainEvent.NavigateTo(MainNavigation.DropInWithCustomSession(session, dropInConfiguration))
                )
            } else {
                onError("Something went wrong while starting session")
            }
        }
    }

    private suspend fun getPaymentMethods() = paymentsRepository.getPaymentMethods(
        getPaymentMethodRequest(
            merchantAccount = keyValueStorage.getMerchantAccount(),
            shopperReference = keyValueStorage.getShopperReference(),
            amount = keyValueStorage.getAmount(),
            countryCode = keyValueStorage.getCountry(),
            shopperLocale = keyValueStorage.getShopperLocale(),
            splitCardFundingSources = keyValueStorage.isSplitCardFundingSources()
        )
    )

    private suspend fun getSession(dropInConfiguration: DropInConfiguration): CheckoutSession? {
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
                redirectUrl = savedStateHandle.get<String>(MainActivity.RETURN_URL_EXTRA)
                    ?: throw IllegalStateException("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode())
            )
        ) ?: return null

        return getCheckoutSession(sessionModel, dropInConfiguration)
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        dropInConfiguration: DropInConfiguration
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, dropInConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> {
                onError("Something went wrong while starting session")
                null
            }
        }
    }

    private fun onError(message: String) {
        _viewState.tryEmit(getComponentItemsViewState())
        _eventFlow.tryEmit(MainEvent.Toast(message))
    }

    private fun getComponentItemsViewState() = MainViewState.Result(ComponentItemProvider.getComponentItems())

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
        Logger.d(TAG, "onCleared")
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
