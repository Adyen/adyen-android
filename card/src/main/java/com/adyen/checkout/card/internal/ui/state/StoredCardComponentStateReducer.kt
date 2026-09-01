/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.answersFocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.applyFocusChange
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalid

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
        id: StoredCardFieldId,
        hasFocus: Boolean,
    ): StoredCardComponentState {
        val updated = updateTextInput(id) { field -> field.applyFocusChange(form, id, hasFocus) }

        return if (form.answersFocusRequest(id, hasFocus)) updated.copy(focusRequest = null) else updated
    }

    private fun highlightValidationErrors(state: StoredCardComponentState): StoredCardComponentState {
        val highlighted = StoredCardFieldId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(
            focusRequest = state.form.requestFocusOnFirstInvalid { state.isFieldValid(it) },
        )
    }

    private fun StoredCardComponentState.isFieldValid(id: StoredCardFieldId): Boolean = when (id) {
        StoredCardFieldId.SECURITY_CODE -> securityCode.isValid
    }
}
