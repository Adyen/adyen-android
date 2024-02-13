/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/2/2024.
 */

package com.adyen.checkout.demo.ui

import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.demo.model.StoreItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MyStoreViewModel @Inject constructor(
) : ViewModel() {

    private val _myStoreState = MutableStateFlow(MyStoreState())
    val myStoreState: StateFlow<MyStoreState> = _myStoreState.asStateFlow()

    private val shoppingCart: StoreItem?
        get() = _myStoreState.value.shoppingCart

    fun addToCart(storeItem: StoreItem) {
        if (shoppingCart == null) {
            _myStoreState.update {
                it.copy(shoppingCart = storeItem)
            }
        }
    }

    fun removeFromCart(storeItem: StoreItem) {
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
