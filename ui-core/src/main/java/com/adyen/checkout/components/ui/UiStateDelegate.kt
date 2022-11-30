/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.components.ui

import kotlinx.coroutines.flow.Flow

interface UiStateDelegate {

    val uiStateFlow: Flow<PaymentComponentUiState>

    val uiEventFlow: Flow<PaymentComponentUiEvent>
}
