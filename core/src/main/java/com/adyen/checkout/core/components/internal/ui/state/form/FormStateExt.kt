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
 * Returns a request for the first text input, or null if the form has none. Pickers and switches are skipped, so a form
 * that starts with a picker still opens on the field below it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstTextInput(): FocusRequest<Id>? =
    elements.firstOrNull { it.id.isTextInput }?.let { FocusRequest(id = it.id) }

/**
 * Returns the next text input after [id], or null if [id] is the last one or is not in the form. Pickers and switches
 * are skipped, because this answers on a keyboard's behalf.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.nextTextInputAfter(id: Id): Id? {
    val index = elements.indexOfFirst { it.id == id }
    if (index == -1) return null
    return elements.subList(index + 1, elements.size).firstOrNull { it.id.isTextInput }?.id
}

/**
 * Returns DONE for the last text input and NEXT for every other element.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.keyboardActionFor(id: Id): KeyboardAction {
    val lastTextInputId = elements.lastOrNull { it.id.isTextInput }?.id
    return if (id == lastTextInputId) KeyboardAction.DONE else KeyboardAction.NEXT
}

/**
 * Returns a request for the first invalid element, or null if all of them are valid. Call this when the shopper presses
 * pay. The request keeps the error highlight, so the field it lands on shows what is wrong.
 *
 * Do not call this from a reducer that changed a field's text or requirement policy. Validation runs after the
 * reducer, so the validity read here is from before that change.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstInvalid(): FocusRequest<Id>? {
    val firstInvalidElement = elements.firstOrNull { !it.isValid }
    return firstInvalidElement?.let { FocusRequest(id = it.id, showErrorIfPresent = true) }
}
