/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalid
import com.adyen.checkout.core.components.internal.ui.state.model.applyFocusChange

internal class StoredCardComponentStateReducer : ComponentStateReducer<StoredCardComponentState, StoredCardIntent> {

    override fun reduce(state: StoredCardComponentState, intent: StoredCardIntent): StoredCardComponentState {
        return when (intent) {
            is StoredCardIntent.UpdateSecurityCode -> state.copy(
                securityCode = state.securityCode.updateText(intent.securityCode),
            )

            is StoredCardIntent.UpdateFieldFocus -> state.updateFieldFocus(intent.id, intent.hasFocus)

            is StoredCardIntent.FocusRequestConsumed -> if (state.focusRequest?.id == intent.id) {
                state.copy(focusRequest = null)
            } else {
                state
            }

            is StoredCardIntent.UpdateLoading -> state.copy(
                isLoading = intent.isLoading,
            )

            is StoredCardIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    private fun StoredCardComponentState.updateFieldFocus(
        id: StoredCardFormElementId,
        hasFocus: Boolean,
    ): StoredCardComponentState = updateTextInput(id) { field -> field.applyFocusChange(focusRequest, id, hasFocus) }

    private fun highlightValidationErrors(state: StoredCardComponentState): StoredCardComponentState {
        val highlighted = StoredCardFormElementId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(focusRequest = state.form.requestFocusOnFirstInvalid())
    }
}
