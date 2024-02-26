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
import com.adyen.checkout.demo.data.api.model.Country
import com.adyen.checkout.demo.data.api.model.getSessionRequest
import com.adyen.checkout.demo.data.model.CartItem
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
import javax.inject.Inject

@HiltViewModel
class MyStoreDemoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionsRepository: SessionsRepository,
    private val myStoreDemoConfigurationProvider: MyStoreDemoConfigurationProvider,
) : ViewModel() {

    private val _myStoreState = MutableStateFlow(
        MyStoreState(
            shoppingCart = emptyList(),
            uiState = MyStoreDemoUiState.Shopping,
            country = Country.NL,
            storeItems = MOCK_STORE_ITEMS,
        ),
    )
    val myStoreState: StateFlow<MyStoreState> = _myStoreState.asStateFlow()

    private val shoppingCart: List<CartItem>
        get() = _myStoreState.value.shoppingCart

    private val amount: Amount
        get() {
            val total = shoppingCart
                .map { (item, count) -> item.price.value * count }
                .reduce { acc, l -> acc + l }
            return Amount(currency = country.currencyCode, total)
        }

    private val country: Country
        get() = _myStoreState.value.country

    fun startDropIn() {
        viewModelScope.launch { fetchSession() }
    }

    private suspend fun fetchSession() = withContext(Dispatchers.IO) {
        val config = myStoreDemoConfigurationProvider.getCheckoutConfiguration(amount)
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
                amount = amount,
                countryCode = country.name,
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
                    it.copy(shoppingCart = emptyList())
                }
                PaymentResultState.Success
            }

            null -> PaymentResultState.Error
        }
        _myStoreState.update {
            it.copy(uiState = MyStoreDemoUiState.Result(resultState))
        }
    }

    fun resultConsumed() {
        _myStoreState.update {
            it.copy(uiState = MyStoreDemoUiState.Shopping)
        }
    }

    fun addToCart(storeItem: StoreItem) {
        val updatedShoppingCart = if (shoppingCart.map { it.storeItem }.contains(storeItem)) {
            shoppingCart.map { (item, count) ->
                if (item == storeItem) {
                    CartItem(storeItem = item, count = count + 1)
                } else {
                    CartItem(storeItem = item, count = count)
                }
            }
        } else {
            shoppingCart + CartItem(storeItem, 1)
        }
        _myStoreState.update {
            it.copy(
                shoppingCart = updatedShoppingCart,
            )
        }
    }

    fun removeFromCart(storeItem: StoreItem) {
        val updatedShoppingCart = shoppingCart.mapNotNull { (item, count) ->
            if (item == storeItem) {
                if (count > 1) {
                    CartItem(storeItem = item, count = count - 1)
                } else {
                    null
                }
            } else {
                CartItem(storeItem = item, count = count)
            }
        }
        _myStoreState.update {
            it.copy(
                shoppingCart = updatedShoppingCart,
            )
        }
    }

    fun updateCountry(country: Country) {
        _myStoreState.update {
            it.copy(
                country = country,
                storeItems = updatedStoredItems(country.currencyCode),
                shoppingCart = shoppingCart.map { (item, count) ->
                    CartItem(
                        storeItem = item.copy(
                            price = Amount(
                                currency = country.currencyCode,
                                value = item.price.value,
                            ),
                        ),
                        count = count,
                    )
                },
            )
        }
    }

    private fun updatedStoredItems(currencyCode: String) = MOCK_STORE_ITEMS.map {
        it.copy(price = Amount(currency = currencyCode, value = it.price.value))
    }

    companion object {
        private const val PRICE_SHIRT = 24_99L
        private const val PRICE_TICKET = 39_99L
        private const val PRICE_BOOTS = 35_99L
        private const val PRICE_SUNGLASSES = 14_99L
        private const val PRICE_HEADPHONES = 19_99L
        private const val PRICE_BACKPACK = 35_99L
        private const val PRICE_JOYPAD = 23_99L
        private const val PRICE_FOOD = 18_99L
        private const val PRICE_HANDBAG = 499_99L

        private val MOCK_STORE_ITEMS = listOf(
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
            StoreItem(
                "Headphones",
                "https://www.mystoredemo.io/a7504eee284c774d0a73.png",
                Amount("EUR", PRICE_HEADPHONES),
            ),
            StoreItem(
                "Backpack",
                "https://www.mystoredemo.io/8db885d088ed05f4a2c1.png",
                Amount("EUR", PRICE_BACKPACK),
            ),
            StoreItem(
                "Joypad",
                "https://www.mystoredemo.io/6df62995d39927d21ba1.png",
                Amount("EUR", PRICE_JOYPAD),
            ),
            StoreItem(
                "Food Delivery",
                "https://www.mystoredemo.io/74a7fcc9408bb3887c31.png",
                Amount("EUR", PRICE_FOOD),
            ),
            StoreItem(
                "Handbag",
                "https://www.mystoredemo.io/6ad4988f7468c17c268e.png",
                Amount("EUR", PRICE_HANDBAG),
            ),
        )
    }
}
