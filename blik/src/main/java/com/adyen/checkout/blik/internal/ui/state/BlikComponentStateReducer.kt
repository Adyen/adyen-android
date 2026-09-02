/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalid
import com.adyen.checkout.core.components.internal.ui.state.model.applyFocusChange

internal class BlikComponentStateReducer : ComponentStateReducer<BlikComponentState, BlikIntent> {

    override fun reduce(state: BlikComponentState, intent: BlikIntent): BlikComponentState {
        return when (intent) {
            is BlikIntent.UpdateBlikCode -> state.copy(
                blikCode = state.blikCode.updateText(intent.code),
            )

            is BlikIntent.UpdateFieldFocus -> state.updateFieldFocus(intent.id, intent.hasFocus)

            is BlikIntent.FocusRequestConsumed -> if (state.focusRequest?.id == intent.id) {
                state.copy(focusRequest = null)
            } else {
                state
            }

            is BlikIntent.UpdateLoading -> state.copy(isLoading = intent.isLoading)

            is BlikIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    private fun BlikComponentState.updateFieldFocus(id: BlikFormElementId, hasFocus: Boolean): BlikComponentState =
        updateTextInput(id) { field -> field.applyFocusChange(focusRequest, id, hasFocus) }

    private fun highlightValidationErrors(state: BlikComponentState): BlikComponentState {
        val highlighted = BlikFormElementId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(focusRequest = state.form.requestFocusOnFirstInvalid())
    }
}
