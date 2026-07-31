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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class TextInputComponentState(
    val text: String = "",
    val description: CheckoutLocalizationKey? = null,
    val errorMessage: CheckoutLocalizationKey? = null,
    val isFocused: Boolean = false,
    val showError: Boolean = false,
    val requirementPolicy: RequirementPolicy = RequirementPolicy.Required,
) {

    val isValid: Boolean
        get() = errorMessage == null

    /**
     * Whether the error message should be displayed, which requires both an error to be present and the field to be
     * showing its errors.
     */
    val showErrorMessage: Boolean
        get() = showError && errorMessage != null

    fun updateText(text: String) = copy(text = text, showError = false)

    fun updateFocus(hasFocus: Boolean) = copy(
        isFocused = hasFocus,
        showError = !hasFocus,
    )
}
