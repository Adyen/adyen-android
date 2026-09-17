/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class StoredCardComponentStateReducer : ComponentStateReducer<StoredCardComponentState, StoredCardIntent> {

    override fun reduce(state: StoredCardComponentState, intent: StoredCardIntent): StoredCardComponentState {
        return when (intent) {
            is StoredCardIntent.UpdateSecurityCode -> state.copy(
                securityCode = state.securityCode.updateText(intent.securityCode),
            )

            is StoredCardIntent.UpdateLoading -> state.copy(
                isLoading = intent.isLoading,
            )

            is StoredCardIntent.UpdateFieldFocus,
            is StoredCardIntent.FocusRequestConsumed,
            is StoredCardIntent.HighlightValidationErrors -> state
        }
    }
}
