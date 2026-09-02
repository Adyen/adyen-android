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
     * An error on the field, together with whether the shopper should see it yet.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class InputError(
        val message: CheckoutLocalizationKey,
        val isVisible: Boolean = false,
    )

    val isValid: Boolean
        get() = error == null

    /**
     * Whether the shopper can see this field at all. A hidden field is not on screen, so it is neither rendered nor
     * one of its form's elements.
     */
    internal val isVisible: Boolean
        get() = requirementPolicy != RequirementPolicy.Hidden

    val isErrorVisible: Boolean
        get() = error?.isVisible == true

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

    /**
     * Shows the error, if there is one.
     */
    fun showErrorIfPresent() = copy(error = error?.copy(isVisible = true))

    /**
     * Hides the error, if there is one.
     */
    fun hideErrorIfPresent() = copy(error = error?.copy(isVisible = false))
}

/**
 * Returns this field as it should be after gaining or losing focus, which is a question of whether the shopper is now
 * shown the error it is holding.
 *
 * A focus gain normally means the shopper tapped the field, and a field the shopper is working on should not be showing
 * an error. The exception is focus the form asked for after the shopper pressed pay: the point of that move is to show
 * the shopper what is wrong. Losing focus is the same event whatever caused it.
 *
 * Every component decides this the same way, so the rule lives here. Applying it to the right field does not, because
 * only a component knows which property an element id names.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> TextInputComponentState.applyFocusChange(
    focusRequest: FocusRequest<Id>?,
    id: Id,
    hasFocus: Boolean,
): TextInputComponentState = when {
    // Whatever moved focus away, the shopper has finished with the field and can be told what is wrong with it.
    !hasFocus -> showErrorIfPresent()

    // Focus that follows pay exists to point at the error, so it shows it rather than clearing it. Highlighting has
    // already made the error visible by this point, so this only has to avoid undoing that.
    focusRequest?.takeIf { it.id == id }?.keepErrorHighlight == true -> showErrorIfPresent()

    // The shopper is moving into the field, either by tapping it or because a prefill sent them there. They should not
    // be greeted by an error about what they are about to change.
    else -> hideErrorIfPresent()
}
