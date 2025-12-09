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
data class TextInputViewState(
    val text: String = "",
    val supportingText: CheckoutLocalizationKey? = null,
    val isFocused: Boolean = false,
    val isError: Boolean = false,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.toViewState(): TextInputViewState {
    val isError = showError && errorMessage != null
    return TextInputViewState(
        text = text,
        supportingText = if (isError) errorMessage else description,
        isFocused = isFocused,
        isError = isError,
    )
}
