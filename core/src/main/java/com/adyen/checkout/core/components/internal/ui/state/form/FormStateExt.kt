/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import androidx.annotation.RestrictTo

/**
 * Returns a request for the first invalid text input, or null if there is none. Pickers and switches are skipped.
 * If [afterElementId] is null or absent from the form, the search starts at the first element.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstInvalidTextInput(
    showErrorIfPresent: Boolean,
    afterElementId: Id? = null,
): FocusRequest<Id>? {
    val afterElementIndex = elements.indexOfFirst { it.id == afterElementId }

    val firstInvalidTextInput = elements
        .drop(afterElementIndex + 1)
        .firstOrNull { it.id.isTextInput && !it.isValid }
    return firstInvalidTextInput?.let {
        FocusRequest(id = it.id, showErrorIfPresent = showErrorIfPresent)
    }
}

/**
 * Returns DONE for the last text input and NEXT for every other element.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.keyboardActionFor(id: Id): KeyboardAction {
    val lastTextInputId = elements.lastOrNull { it.id.isTextInput }?.id
    return if (id == lastTextInputId) KeyboardAction.DONE else KeyboardAction.NEXT
}
