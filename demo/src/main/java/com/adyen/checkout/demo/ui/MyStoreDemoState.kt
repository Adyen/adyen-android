/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.ui

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.demo.data.api.model.Country
import com.adyen.checkout.demo.data.model.StoreItem
import com.adyen.checkout.sessions.core.CheckoutSession

data class MyStoreState(
    val shoppingCart: List<StoreItem>,
    val uiState: MyStoreDemoUiState,
    val country: Country,
    val storeItems: List<StoreItem>,
    val isCartFull: Boolean
)

sealed class MyStoreDemoUiState {
    object Shopping : MyStoreDemoUiState()
    object Loading : MyStoreDemoUiState()
    object Error : MyStoreDemoUiState()
    data class StartDropIn(
        val session: CheckoutSession,
        val checkoutConfiguration: CheckoutConfiguration
    ) : MyStoreDemoUiState()

    data class Result(val state: PaymentResultState) : MyStoreDemoUiState()
}

sealed class PaymentResultState {
    data object Cancelled : PaymentResultState()
    data object Error : PaymentResultState()
    data object Success : PaymentResultState()
}
