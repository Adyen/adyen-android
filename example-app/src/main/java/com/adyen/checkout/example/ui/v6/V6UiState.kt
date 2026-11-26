/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/8/2025.
 */

package com.adyen.checkout.example.ui.v6

import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.RegularPaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethodResponse
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.compose.UIText

sealed interface V6UiState {

    data class Component(
        val paymentMethods: List<RegularPaymentMethodResponse>,
        val storedPaymentMethods: List<StoredPaymentMethodResponse>,
        val selectedPaymentMethod: PaymentMethodResponse,
        val checkoutController: CheckoutController,
        val showCustomButton: Boolean,
        val isHandlingAction: Boolean = false,
    ) : V6UiState

    data object Loading : V6UiState

    data class Error(
        val message: UIText,
    ) : V6UiState

    data class Final(
        val resultState: ResultState,
    ) : V6UiState
}
