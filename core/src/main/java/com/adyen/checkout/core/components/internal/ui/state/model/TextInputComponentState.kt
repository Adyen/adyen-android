/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/9/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class TextInputComponentState(
    val text: String = "",
    val description: CheckoutLocalizationKey? = null,
    val error: InputError? = null,
    val requirementPolicy: RequirementPolicy = RequirementPolicy.Required,
) {

    /**
     * An error and whether the shopper should see it yet. Validation finds a problem long before the shopper is ready
     * to be told about it.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class InputError(
        val message: CheckoutLocalizationKey,
        val isVisible: Boolean = false,
    )

    val isValid: Boolean = error == null

    /** A hidden field is not rendered and is not one of its form's elements. */
    internal val isVisible: Boolean = requirementPolicy != RequirementPolicy.Hidden

    val isErrorVisible: Boolean = error?.isVisible == true

    /** Typing hides the error, so the shopper is not corrected while fixing the thing they were corrected about. */
    fun updateText(text: String) = copy(text = text).hideErrorIfPresent()

    /**
     * Replaces the error, keeping whether it is currently visible. Validators call this on every pass, so replacing a
     * message must not hide an error the shopper is already looking at.
     */
    fun updateError(message: CheckoutLocalizationKey?): TextInputComponentState {
        return when {
            message == null -> copy(error = null)
            error == null -> copy(error = InputError(message, isVisible = false))
            else -> copy(error = InputError(message, isVisible = error.isVisible))
        }
    }

    fun showErrorIfPresent() = copy(error = error?.copy(isVisible = true))

    fun hideErrorIfPresent() = copy(error = error?.copy(isVisible = false))
}

/**
 * This field after gaining or losing focus, which comes down to whether the shopper is now shown the error it holds.
 *
 * A gain normally means the shopper tapped the field, and a field being worked on should not be showing an error. The
 * exception is focus that follows pay, which exists to show the error. A loss is the same event whatever caused it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> TextInputComponentState.applyFocusChange(
    focusRequest: FocusRequest<Id>?,
    id: Id,
    hasFocus: Boolean,
): TextInputComponentState = when {
    !hasFocus -> showErrorIfPresent()
    focusRequest?.takeIf { it.id == id }?.keepErrorHighlight == true -> showErrorIfPresent()
    else -> hideErrorIfPresent()
}
