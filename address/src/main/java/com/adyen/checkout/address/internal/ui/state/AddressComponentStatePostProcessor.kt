/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStatePostProcessor
import com.adyen.checkout.core.components.internal.ui.state.form.requestFocusOnFirstInvalidTextInput
import com.adyen.checkout.core.components.internal.ui.state.model.updateErrorVisibility

internal class AddressComponentStatePostProcessor :
    ComponentStatePostProcessor<AddressComponentState, AddressIntent> {

    override fun processInitialState(state: AddressComponentState) = state.focusFirstInvalid(showErrorIfPresent = false)

    override fun process(
        previousState: AddressComponentState,
        currentState: AddressComponentState,
        intent: AddressIntent,
    ) = when (intent) {
        is AddressIntent.UpdateFieldFocus -> currentState.updateErrorVisibility(intent.id, intent.hasFocus)
        is AddressIntent.FocusRequestConsumed -> currentState.clearFocusRequest(intent.id)

        is AddressIntent.UpdateCountry ->
            currentState.focusFirstInvalid(showErrorIfPresent = false, afterElementId = AddressFormElementId.COUNTRY)

        is AddressIntent.Prefill -> currentState.focusFirstInvalid(showErrorIfPresent = false)

        is AddressIntent.HighlightValidationErrors ->
            currentState.showAllErrors().focusFirstInvalid(showErrorIfPresent = true)

        is AddressIntent.UpdatePostalCode -> currentState
    }

    private fun AddressComponentState.focusFirstInvalid(
        showErrorIfPresent: Boolean,
        afterElementId: AddressFormElementId? = null,
    ) = copy(
        focusRequest = form.requestFocusOnFirstInvalidTextInput(
            showErrorIfPresent = showErrorIfPresent,
            afterElementId = afterElementId,
        ),
    )

    private fun AddressComponentState.clearFocusRequest(id: AddressFormElementId) =
        if (focusRequest?.id == id) copy(focusRequest = null) else this

    private fun AddressComponentState.updateErrorVisibility(id: AddressFormElementId, hasFocus: Boolean) =
        updateTextInput(id) { field -> field.updateErrorVisibility(focusRequest, id, hasFocus) }

    private fun AddressComponentState.showAllErrors() = copy(
        country = country.showErrorIfPresent(),
        postalCode = postalCode.showErrorIfPresent(),
    )
}
