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
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstInvalidTextInput(
    showErrorIfPresent: Boolean,
): FocusRequest<Id>? {
    val firstInvalidTextInput = elements.firstOrNull { it.id.isTextInput && !it.isValid }
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
