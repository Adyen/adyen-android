/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/11/2023.
 */

package com.adyen.checkout.example.ui.card

import androidx.compose.runtime.Immutable
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.core.action.Action

@Immutable
internal data class SessionsCardUiState(
    val cardConfiguration: CardConfiguration,
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val componentData: SessionsCardComponentData? = null,
    val action: Action? = null,
)
