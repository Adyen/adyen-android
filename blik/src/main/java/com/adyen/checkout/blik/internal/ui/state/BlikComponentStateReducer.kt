/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class BlikComponentStateReducer : ComponentStateReducer<BlikComponentState, BlikIntent> {

    override fun reduce(state: BlikComponentState, intent: BlikIntent): BlikComponentState {
        return when (intent) {
            is BlikIntent.UpdateBlikCode -> state.copy(
                blikCode = state.blikCode.updateText(intent.code),
            )

            is BlikIntent.UpdateBlikCodeFocus -> state.copy(
                blikCode = state.blikCode.updateFocus(intent.hasFocus),
            )

            is BlikIntent.UpdateLoading -> state.copy(isLoading = intent.isLoading)

            is BlikIntent.HighlightValidationErrors -> {
                val hasBlikCodeError = state.blikCode.errorMessage != null
                state.copy(
                    blikCode = state.blikCode.copy(
                        showError = hasBlikCodeError,
                        isFocused = hasBlikCodeError,
                    ),
                )
            }
        }
    }
}
