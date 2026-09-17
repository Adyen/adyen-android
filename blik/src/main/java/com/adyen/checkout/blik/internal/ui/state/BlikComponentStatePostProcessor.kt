/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStatePostProcessor
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalidTextInput
import com.adyen.checkout.core.components.internal.ui.state.model.updateErrorVisibility

internal class BlikComponentStatePostProcessor : ComponentStatePostProcessor<BlikComponentState, BlikIntent> {

    override fun processInitialState(state: BlikComponentState) = state.focusFirstInvalid(showErrorIfPresent = false)

    override fun process(state: BlikComponentState, intent: BlikIntent) = when (intent) {
        is BlikIntent.UpdateFieldFocus -> state.updateErrorVisibility(intent.id, intent.hasFocus)
        is BlikIntent.FocusRequestConsumed -> state.clearFocusRequest(intent.id)

        is BlikIntent.HighlightValidationErrors ->
            state.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        else -> state
    }

    private fun BlikComponentState.focusFirstInvalid(showErrorIfPresent: Boolean) = copy(
        focusRequest = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = showErrorIfPresent),
    )

    private fun BlikComponentState.clearFocusRequest(id: BlikFormElementId) =
        if (focusRequest?.id == id) copy(focusRequest = null) else this

    private fun BlikComponentState.updateErrorVisibility(id: BlikFormElementId, hasFocus: Boolean) =
        updateTextInput(id) { field -> field.updateErrorVisibility(focusRequest, id, hasFocus) }

    private fun BlikComponentState.showAllErrors() =
        BlikFormElementId.entries.fold(this) { state, id ->
            state.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }
}
