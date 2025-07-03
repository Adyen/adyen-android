/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class DelegateFieldState<T>(
    val value: T,
    val validation: Validation? = null,
    val hasFocus: Boolean = false,
    val shouldHighlightValidationError: Boolean = false,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> DelegateFieldState<T>.updateFieldState(
    value: T? = null,
    validation: Validation? = null,
    hasFocus: Boolean? = null,
    shouldHighlightValidationError: Boolean? = null,
): DelegateFieldState<T> = copy(
    value = value ?: this.value,
    validation = validation ?: this.validation,
    hasFocus = hasFocus ?: this.hasFocus,
    shouldHighlightValidationError = shouldHighlightValidationError
        ?: this.shouldHighlightValidationError,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> DelegateFieldState<T>.toViewFieldState() = ViewFieldState(
    value = value,
    hasFocus = hasFocus,
    errorMessageId = takeIf { fieldState ->
        fieldState.shouldShowValidationError()
    }?.validation.let { it as? Validation.Invalid }?.reason,
)

// Validation error should be shown, when the field loses its focus or when we manually trigger a validation
internal fun <T> DelegateFieldState<T>.shouldShowValidationError() =
    !this.hasFocus || this.shouldHighlightValidationError
