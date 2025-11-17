/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/8/2025.
 */

package com.adyen.checkout.example.ui.v6

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.compose.UIText

sealed interface V6UiState {

    data class Component(
        val checkoutContext: CheckoutContext,
        val checkoutCallbacks: CheckoutCallbacks,
        val paymentMethods: List<PaymentMethod>,
    ) : V6UiState

    data object Loading : V6UiState

    data class Error(
        val message: UIText,
    ) : V6UiState

    data class Final(
        val resultState: ResultState,
    ) : V6UiState
}
