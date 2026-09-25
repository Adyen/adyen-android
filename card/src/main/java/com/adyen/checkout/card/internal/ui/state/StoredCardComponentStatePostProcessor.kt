/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStatePostProcessor
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalidTextInput
import com.adyen.checkout.core.components.internal.ui.state.model.updateErrorVisibility

internal class StoredCardComponentStatePostProcessor :
    ComponentStatePostProcessor<StoredCardComponentState, StoredCardIntent> {

    override fun processInitialState(state: StoredCardComponentState) =
        state.focusFirstInvalid(showErrorIfPresent = false)

    override fun process(
        previousState: StoredCardComponentState,
        currentState: StoredCardComponentState,
        intent: StoredCardIntent,
    ) = when (intent) {
        is StoredCardIntent.UpdateFieldFocus -> currentState.updateErrorVisibility(intent.id, intent.hasFocus)
        is StoredCardIntent.FocusRequestConsumed -> currentState.clearFocusRequest(intent.id)

        is StoredCardIntent.HighlightValidationErrors ->
            currentState.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        else -> currentState
    }

    private fun StoredCardComponentState.focusFirstInvalid(showErrorIfPresent: Boolean) = copy(
        focusRequest = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = showErrorIfPresent),
    )

    private fun StoredCardComponentState.clearFocusRequest(id: StoredCardFormElementId) =
        if (focusRequest?.id == id) copy(focusRequest = null) else this

    private fun StoredCardComponentState.updateErrorVisibility(id: StoredCardFormElementId, hasFocus: Boolean) =
        updateTextInput(id) { field -> field.updateErrorVisibility(focusRequest, id, hasFocus) }

    private fun StoredCardComponentState.showAllErrors() =
        StoredCardFormElementId.entries.fold(this) { state, id ->
            state.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }
}
