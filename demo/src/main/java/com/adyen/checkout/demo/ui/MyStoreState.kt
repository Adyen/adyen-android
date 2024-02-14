/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.ui

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.demo.model.StoreItem
import com.adyen.checkout.sessions.core.CheckoutSession

data class MyStoreState(
    val shoppingCart: StoreItem? = null,
    val uiState: MyStoreUiState = MyStoreUiState.Shopping
)

sealed class MyStoreUiState {
    object Shopping : MyStoreUiState()
    object Loading : MyStoreUiState()
    object Error : MyStoreUiState()
    data class StartDropIn(val session: CheckoutSession, val checkoutConfiguration: CheckoutConfiguration) :
        MyStoreUiState()
}
