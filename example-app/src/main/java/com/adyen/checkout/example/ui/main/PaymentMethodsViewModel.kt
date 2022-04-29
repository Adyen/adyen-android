/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.service.getSessionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PaymentMethodsViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
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
        }
    }

    private fun startDropInFlow() {
        viewModelScope.launch {
            _viewState.emit(MainViewState.Loading)

            val paymentMethods = getPaymentMethods()
            if (paymentMethods != null) {
                _viewState.emit(MainViewState.Result(ComponentItemProvider.getComponentItems()))
                _navigateTo.emit(MainNavigation.DropIn(paymentMethods))
            } else {
                _viewState.emit(MainViewState.Error("Something went wrong while fetching payment methods"))
            }
        }
    }

    private fun startSessionDropInFlow() {
        viewModelScope.launch {
            _viewState.emit(MainViewState.Loading)

            val paymentMethodsDef = async { getPaymentMethods() }
            val sessionDef = async { getSession() }

            val paymentMethods = paymentMethodsDef.await()
            val session = sessionDef.await()

            if (paymentMethods != null && session != null) {
                _viewState.emit(MainViewState.Result(ComponentItemProvider.getComponentItems()))
                _navigateTo.emit(MainNavigation.DropInWithSession(session, paymentMethods))
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

    private suspend fun getSession() = paymentsRepository.getSessionAsync(
        getSessionRequest(
            merchantAccount = keyValueStorage.getMerchantAccount(),
            shopperReference = keyValueStorage.getShopperReference(),
            amount = keyValueStorage.getAmount(),
            countryCode = keyValueStorage.getCountry(),
            shopperLocale = keyValueStorage.getShopperLocale(),
            splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
            isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
            // RedirectComponent.getReturnUrl(applicationContext) should be used here
            redirectUrl = "adyencheckout://com.adyen.checkout.example",
        )
    )

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
