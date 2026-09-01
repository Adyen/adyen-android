/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.answersFocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.applyFocusChange
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalid

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

    private fun BlikComponentState.updateFieldFocus(id: BlikFieldId, hasFocus: Boolean): BlikComponentState {
        val updated = updateTextInput(id) { field -> field.applyFocusChange(form, id, hasFocus) }

        return if (form.answersFocusRequest(id, hasFocus)) updated.copy(focusRequest = null) else updated
    }

    private fun highlightValidationErrors(state: BlikComponentState): BlikComponentState {
        val highlighted = BlikFieldId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(
            focusRequest = state.form.requestFocusOnFirstInvalid { state.isFieldValid(it) },
        )
    }

    private fun BlikComponentState.isFieldValid(id: BlikFieldId): Boolean = when (id) {
        BlikFieldId.BLIK_CODE -> blikCode.isValid
    }
}
