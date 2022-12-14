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
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.example.ui.main.MainActivity.Companion.RETURN_URL_EXTRA
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.model.SessionModel
import com.adyen.checkout.sessions.provider.CheckoutSessionProvider
import com.adyen.checkout.sessions.provider.CheckoutSessionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel() {

    private val _viewState =
        MutableStateFlow<MainViewState>(MainViewState.Result(ComponentItemProvider.getComponentItems()))
    val viewState: Flow<MainViewState> = _viewState

    private val _navigateTo = MutableSharedFlow<MainNavigation>(extraBufferCapacity = 1)
    val navigateTo: Flow<MainNavigation> = _navigateTo

    fun onComponentEntryClick(entry: ComponentItem.Entry) {
        when (entry) {
            ComponentItem.Entry.Card -> _navigateTo.tryEmit(MainNavigation.Card)
            ComponentItem.Entry.DropIn -> startDropInFlow()
            ComponentItem.Entry.DropInWithSession -> startSessionDropInFlow()
            ComponentItem.Entry.DropInWithCustomSession -> startCustomSessionDropInFlow()
            ComponentItem.Entry.Blik -> _navigateTo.tryEmit(MainNavigation.Blik)
        }
    }

    private fun startDropInFlow() {
        viewModelScope.launch {
            _viewState.emit(MainViewState.Loading)

            val paymentMethods = getPaymentMethods()
            if (paymentMethods != null) {
                _viewState.emit(MainViewState.Result(ComponentItemProvider.getComponentItems()))
                val dropInConfiguration = checkoutConfigurationProvider.getDropInConfiguration()
                _navigateTo.emit(MainNavigation.DropIn(paymentMethods, dropInConfiguration))
            } else {
                _viewState.emit(MainViewState.Error("Something went wrong while fetching payment methods"))
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
                _navigateTo.emit(MainNavigation.DropInWithSession(session, dropInConfiguration))
            } else {
                _viewState.emit(MainViewState.Error("Something went wrong while starting session"))
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
                _navigateTo.emit(MainNavigation.DropInWithCustomSession(session, dropInConfiguration))
            } else {
                _viewState.emit(MainViewState.Error("Something went wrong while starting session"))
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
        val sessionModel = paymentsRepository.getSessionAsync(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
                redirectUrl = savedStateHandle.get<String>(RETURN_URL_EXTRA)
                    ?: throw IllegalStateException("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail()
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
                _viewState.emit(MainViewState.Error("Something went wrong while starting session"))
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
