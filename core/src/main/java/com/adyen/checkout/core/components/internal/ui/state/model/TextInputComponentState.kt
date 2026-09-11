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
     * An error and whether the shopper should see it yet. The two are held together so a field cannot show an error it
     * does not have.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class InputError(
        val message: CheckoutLocalizationKey,
        val isVisible: Boolean = false,
    )

    val isErrorVisible: Boolean
        get() = error?.isVisible == true

    /** Typing hides the error, so the shopper is not corrected while fixing the thing they were corrected about. */
    fun updateText(text: String) = copy(text = text).hideErrorIfPresent()

    /**
     * Replaces the error, keeping whether it is currently visible. Use this from a validator: it runs on every pass,
     * and replacing a message must not hide an error the shopper is already reading.
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
 * Call this on every focus change, gain or loss, whatever caused it. Gaining focus hides the error, because the
 * shopper is working on the field, unless [focusRequest] asked for this field and set showErrorIfPresent. Losing
 * focus shows the error.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> TextInputComponentState.applyFocusChange(
    focusRequest: FocusRequest<Id>?,
    id: Id,
    hasFocus: Boolean,
): TextInputComponentState = when {
    !hasFocus -> showErrorIfPresent()
    focusRequest?.takeIf { it.id == id }?.showErrorIfPresent == true -> showErrorIfPresent()
    else -> hideErrorIfPresent()
}
