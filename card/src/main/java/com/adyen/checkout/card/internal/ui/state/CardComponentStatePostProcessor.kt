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

    override fun process(state: CardComponentState, intent: CardIntent) = when (intent) {
        is CardIntent.UpdateFieldFocus -> state.updateErrorVisibility(intent.id, intent.hasFocus)
        is CardIntent.FocusRequestConsumed -> state.clearFocusRequest(intent.id)
        is CardIntent.UpdateCardScanResult -> state.focusFirstInvalid(showErrorIfPresent = false)

        is CardIntent.HighlightValidationErrors ->
            state.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        else -> state
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
