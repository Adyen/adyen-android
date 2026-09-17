/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStatePostProcessor
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalidTextInput
import com.adyen.checkout.core.components.internal.ui.state.model.updateErrorVisibility

internal class MBWayComponentStatePostProcessor : ComponentStatePostProcessor<MBWayComponentState, MBWayIntent> {

    override fun processInitialState(state: MBWayComponentState) = state.focusFirstInvalid(showErrorIfPresent = false)

    override fun process(state: MBWayComponentState, intent: MBWayIntent) = when (intent) {
        is MBWayIntent.UpdateFieldFocus -> state.updateErrorVisibility(intent.id, intent.hasFocus)
        is MBWayIntent.FocusRequestConsumed -> state.clearFocusRequest(intent.id)

        is MBWayIntent.HighlightValidationErrors ->
            state.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        else -> state
    }

    private fun MBWayComponentState.focusFirstInvalid(showErrorIfPresent: Boolean) = copy(
        focusRequest = form.requestFocusOnFirstInvalidTextInput(showErrorIfPresent = showErrorIfPresent),
    )

    private fun MBWayComponentState.clearFocusRequest(id: MBWayFormElementId) =
        if (focusRequest?.id == id) copy(focusRequest = null) else this

    private fun MBWayComponentState.updateErrorVisibility(id: MBWayFormElementId, hasFocus: Boolean) =
        updateTextInput(id) { field -> field.updateErrorVisibility(focusRequest, id, hasFocus) }

    private fun MBWayComponentState.showAllErrors() =
        MBWayFormElementId.entries.fold(this) { state, id ->
            state.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }
}
