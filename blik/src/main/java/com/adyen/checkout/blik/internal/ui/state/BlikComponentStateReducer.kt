/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.firstInvalid

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

    /**
     * A focus gain normally means the shopper tapped the field, and a field the shopper is working on should not be
     * showing an error. The exception is focus we asked for after the shopper pressed pay, which exists precisely to
     * point at an error and so must not clear it.
     *
     * Losing focus is the same event whatever caused it, and always shows an error the field is holding back.
     */
    // TODO - Form fields rollout: will move to core, so that every component shares these rules instead of copying
    // them. Nothing here is about Blik. Only updateTextInput stays behind, because just the Blik state knows which
    // property each id points at.
    private fun BlikComponentState.updateFieldFocus(id: BlikFieldId, hasFocus: Boolean): BlikComponentState {
        val request = focusRequest?.takeIf { it.id == id }
        val updated = updateTextInput(id) { field ->
            when {
                !hasFocus -> field.showErrorIfPresent()
                request?.keepErrorHighlight == true -> field
                else -> field.hideErrorIfPresent()
            }
        }

        // The request has been answered, so it must not outlive the focus change it asked for and make the shopper's
        // next tap on the same field look programmatic.
        return if (hasFocus && request != null) updated.copy(focusRequest = null) else updated
    }

    /**
     * The shopper pressed pay on a form that cannot be submitted: show every error at once, and send focus to the first
     * field that is wrong in the order the shopper reads them.
     */
    // TODO - Form fields rollout: will move to core together with updateFieldFocus. Nothing here is about Blik either.
    // Only isFieldValid stays behind.
    private fun highlightValidationErrors(state: BlikComponentState): BlikComponentState {
        val firstInvalid = state.form.firstInvalid { state.isFieldValid(it) }

        val highlighted = BlikFieldId.entries.fold(state) { current, id ->
            current.updateTextInput(id) { field -> field.showErrorIfPresent() }
        }

        return highlighted.copy(
            focusRequest = firstInvalid?.let { FocusRequest(id = it, keepErrorHighlight = true) },
        )
    }

    private fun BlikComponentState.isFieldValid(id: BlikFieldId): Boolean = when (id) {
        BlikFieldId.BLIK_CODE -> blikCode.isValid
    }
}
