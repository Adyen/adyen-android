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
data class TextInputState(
    val text: String = "",
    val errorMessage: CheckoutLocalizationKey? = null,
    val isFocused: Boolean = false,
    val isInteractedWith: Boolean = false,
    val showError: Boolean = false,
) {

    val isValid: Boolean
        get() = isInteractedWith && errorMessage == null

    fun updateText(text: String) = copy(text = text, isInteractedWith = true, showError = false)

    fun updateFocus(hasFocus: Boolean) = copy(
        isFocused = hasFocus,
        showError = !hasFocus && isInteractedWith,
        isInteractedWith = true,
    )
}
