/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/1/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class ComponentFieldDelegateState<T>(
    val value: T,
    val validation: Validation? = null,
    // Can we move this up to the delegate state and it just keeps the fieldId of the focused field?
    val hasFocus: Boolean = false,
    val shouldHighlightValidationError: Boolean = false,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> ComponentFieldDelegateState<T>.updateFieldState(
    value: T? = null,
    validation: Validation? = null,
    hasFocus: Boolean? = null,
    shouldHighlightValidationError: Boolean? = null,
): ComponentFieldDelegateState<T> = copy(
    value = value ?: this.value,
    validation = validation ?: this.validation,
    hasFocus = hasFocus ?: this.hasFocus,
    shouldHighlightValidationError = shouldHighlightValidationError ?: this.shouldHighlightValidationError,
)
