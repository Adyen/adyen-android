/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/9/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class TextInputComponentState(
    val text: String = "",
    val description: CheckoutLocalizationKey? = null,
    val error: InputError? = null,
    val isFocused: Boolean = false,
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
    @VisibleForTesting
    fun hideErrorIfPresent() = copy(error = error?.copy(isVisible = false))

    fun updateFocus(hasFocus: Boolean): TextInputComponentState {
        val focused = copy(isFocused = hasFocus)
        return if (hasFocus) focused.hideErrorIfPresent() else focused.showErrorIfPresent()
    }
}
