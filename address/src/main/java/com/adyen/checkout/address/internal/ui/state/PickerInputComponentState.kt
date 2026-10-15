/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState.InputError

// TODO - Address: Non-text elements support neither focus nor scrolling yet, so an invalid picker only shows its
//  error. See "Concerns and follow-up actions" in ADR-0003.
/**
 * The state of a form element the shopper fills by picking a value from a list, such as the country.
 *
 * Unlike a text input it cannot take focus, so its error only becomes visible when validation errors are highlighted.
 */
internal data class PickerInputComponentState<T>(
    val selected: T? = null,
    val error: InputError? = null,
) {

    /**
     * Replaces the error, keeping whether it is currently visible. Use this from a validator: replacing a message must
     * not hide an error the shopper is already reading.
     */
    fun updateError(message: CheckoutLocalizationKey?): PickerInputComponentState<T> = copy(
        error = message?.let { InputError(it, isVisible = error?.isVisible == true) },
    )

    fun showErrorIfPresent(): PickerInputComponentState<T> = copy(error = error?.copy(isVisible = true))
}

/**
 * Returns this picker as a form element. Unlike a text input it has no requirement policy, so whether it is visible is
 * decided by the caller.
 */
internal fun <Id : FormElementId> PickerInputComponentState<*>.toFormElement(id: Id): FormElementState<Id> =
    FormElementState(id = id, isValid = error == null)
