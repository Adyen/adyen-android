/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.remainingAfter
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalid
import com.adyen.checkout.core.components.internal.ui.state.model.applyFocusChange

internal class MBWayComponentStateReducer : ComponentStateReducer<MBWayComponentState, MBWayIntent> {

    override fun reduce(state: MBWayComponentState, intent: MBWayIntent): MBWayComponentState {
        return when (intent) {
            is MBWayIntent.UpdateCountry -> state.copy(selectedCountryCode = intent.country)

            is MBWayIntent.UpdateLoading -> state.copy(isLoading = intent.isLoading)

            is MBWayIntent.UpdatePhoneNumber -> state.copy(
                phoneNumber = state.phoneNumber.updateText(intent.number),
            )

            is MBWayIntent.UpdateFieldFocus -> state.updateFieldFocus(intent.id, intent.hasFocus)

            is MBWayIntent.FocusRequestConsumed -> if (state.focusRequest?.id == intent.id) {
                state.copy(focusRequest = null)
            } else {
                state
            }

            is MBWayIntent.HighlightValidationErrors -> highlightValidationErrors(state)
        }
    }

    private fun MBWayComponentState.updateFieldFocus(id: MBWayFormElementId, hasFocus: Boolean): MBWayComponentState {
        val updated = updateTextInput(id) { field -> field.applyFocusChange(focusRequest, id, hasFocus) }

        return updated.copy(focusRequest = focusRequest.remainingAfter(id, hasFocus))
    }

    private fun highlightValidationErrors(state: MBWayComponentState): MBWayComponentState {
        val highlighted = MBWayFormElementId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(
            focusRequest = state.form.requestFocusOnFirstInvalid(),
        )
    }
}
