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

internal class CardComponentStatePostProcessor : ComponentStatePostProcessor<CardComponentState, CardIntent> {

    override fun processInitialState(state: CardComponentState) = state.focusFirstInvalid(showErrorIfPresent = false)

    override fun process(
        previousState: CardComponentState,
        currentState: CardComponentState,
        intent: CardIntent,
    ) = when (intent) {
        is CardIntent.UpdateFieldFocus -> currentState.updateErrorVisibility(intent.id, intent.hasFocus)
        is CardIntent.FocusRequestConsumed -> currentState.clearFocusRequest(intent.id)
        is CardIntent.UpdateCardScanResult -> currentState.focusFirstInvalid(showErrorIfPresent = false)

        is CardIntent.HighlightValidationErrors ->
            currentState.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        else -> currentState
    }

    private fun CardComponentState.focusFirstInvalid(showErrorIfPresent: Boolean) = copy(
        focusRequest = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = showErrorIfPresent),
    )

    private fun CardComponentState.clearFocusRequest(id: CardFormElementId) =
        if (focusRequest?.id == id) copy(focusRequest = null) else this

    private fun CardComponentState.updateErrorVisibility(id: CardFormElementId, hasFocus: Boolean) =
        updateTextInput(id) { field -> field.updateErrorVisibility(focusRequest, id, hasFocus) }

    private fun CardComponentState.showAllErrors() =
        CardFormElementId.entries.fold(this) { state, id ->
            state.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }
}
