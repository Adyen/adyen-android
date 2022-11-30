/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.components.ui

sealed class PaymentComponentUiState {
    object Loading : PaymentComponentUiState()
    object Idle : PaymentComponentUiState()
}

sealed class PaymentComponentUiEvent {
    object InvalidUI : PaymentComponentUiEvent()
}
