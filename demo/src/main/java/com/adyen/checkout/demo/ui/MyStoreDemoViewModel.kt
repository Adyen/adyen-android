/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/2/2024.
 */

package com.adyen.checkout.demo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.demo.data.api.model.getSessionRequest
import com.adyen.checkout.demo.data.model.StoreItem
import com.adyen.checkout.demo.data.repositories.SessionsRepository
import com.adyen.checkout.demo.ui.configuration.MyStoreDemoConfigurationProvider
import com.adyen.checkout.dropin.SessionDropInResult
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.CheckoutSessionResult
import com.adyen.checkout.sessions.core.SessionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MyStoreDemoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionsRepository: SessionsRepository,
    private val myStoreDemoConfigurationProvider: MyStoreDemoConfigurationProvider,
) : ViewModel() {

    private val _myStoreState = MutableStateFlow(MyStoreState())
    val myStoreState: StateFlow<MyStoreState> = _myStoreState.asStateFlow()

    private val shoppingCart: StoreItem?
        get() = _myStoreState.value.shoppingCart

    fun startDropIn() {
        viewModelScope.launch { fetchSession() }
    }

    private suspend fun fetchSession() = withContext(Dispatchers.IO) {
        val config = myStoreDemoConfigurationProvider.getCheckoutConfiguration(
            shoppingCart?.price ?: Amount(),
        )
        _myStoreState.update {
            it.copy(
                uiState = MyStoreDemoUiState.Loading,
            )
        }
        val checkoutSession = getSession(
            config,
        )
        if (checkoutSession == null) {
            // TODO log error
            _myStoreState.update {
                it.copy(
                    uiState = MyStoreDemoUiState.Error,
                )
            }
            return@withContext
        }

        _myStoreState.update {
            it.copy(
                uiState = MyStoreDemoUiState.StartDropIn(
                    checkoutSession,
                    config,
                ),
            )
        }
    }

    private suspend fun getSession(checkoutConfiguration: CheckoutConfiguration): CheckoutSession? {
        val sessionModel = sessionsRepository.createSession(
            getSessionRequest(
                amount = shoppingCart?.price,
                countryCode = "nl",
                shopperLocale = "en-US",
                splitCardFundingSources = false,
                isExecuteThreeD = true,
                isThreeds2Enabled = true,
                redirectUrl = savedStateHandle.get<String>(MyStoreDemoActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = "",
                installmentOptions = null,
                showInstallmentAmount = false,
            ),
        ) ?: return null

        return getCheckoutSession(
            sessionModel,
            checkoutConfiguration,
        )
    }

    private suspend fun getCheckoutSession(
        sessionModel: SessionModel,
        checkoutConfiguration: CheckoutConfiguration
    ): CheckoutSession? {
        return when (val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
            is CheckoutSessionResult.Success -> result.checkoutSession
            is CheckoutSessionResult.Error -> {
                // TODO log error
                null
            }
        }
    }

    fun onDropInResult(sessionDropInResult: SessionDropInResult?) {
        // TODO send event to show result on ui
        val resultState = when (sessionDropInResult) {
            is SessionDropInResult.CancelledByUser -> PaymentResultState.Cancelled
            is SessionDropInResult.Error -> PaymentResultState.Error
            is SessionDropInResult.Finished -> {
                _myStoreState.update {
                    it.copy(shoppingCart = null)
                }
                PaymentResultState.Success
            }
            null -> PaymentResultState.Error
        }
        _myStoreState.update {
            it.copy(uiState = MyStoreDemoUiState.Result(resultState))
        }
    }

    fun addToCart(storeItem: StoreItem) {
        if (shoppingCart == null) {
            _myStoreState.update {
                it.copy(shoppingCart = storeItem)
            }
        }
    }

    fun removeFromCart() {
        _myStoreState.update {
            it.copy(shoppingCart = null)
        }
    }

    companion object {
        private const val PRICE_SHIRT = 30_00L
        private const val PRICE_TICKET = 50_00L
        private const val PRICE_BOOTS = 40_00L
        private const val PRICE_SUNGLASSES = 15_00L

        val MOCK_STORE_ITEMS = listOf(
            StoreItem(
                "Polo shirt",
                "https://www.mystoredemo.io/1689f3f40b292d1de2c6.png",
                Amount("EUR", PRICE_SHIRT),
            ),
            StoreItem(
                "Event ticket",
                "https://www.mystoredemo.io/2d5aa9e22be92846d23e.png",
                Amount("EUR", PRICE_TICKET),
            ),
            StoreItem(
                "Boots",
                "https://www.mystoredemo.io/6966d3ad4f0f644d236b.png",
                Amount("EUR", PRICE_BOOTS),
            ),
            StoreItem(
                "Sunglasses",
                "https://www.mystoredemo.io/ff0e4b0191980be4a839.png",
                Amount("EUR", PRICE_SUNGLASSES),
            ),
        )
    }
}

fun Amount.formatAmount(locale: Locale): String {
    val currencyCode = currency
    val checkoutCurrency = CheckoutCurrency.find(currencyCode.orEmpty())
    val currency = Currency.getInstance(currencyCode)
    val currencyFormat = DecimalFormat.getCurrencyInstance(locale)
    currencyFormat.currency = currency
    currencyFormat.minimumFractionDigits = checkoutCurrency.fractionDigits
    currencyFormat.maximumFractionDigits = checkoutCurrency.fractionDigits
    val value = BigDecimal.valueOf(value, checkoutCurrency.fractionDigits)
    return currencyFormat.format(value)
}
