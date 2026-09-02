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
 * The request that opens a form on the first field the shopper can type in, or null when it has none — a stored card
 * asking for no security code has an empty form and nothing to focus.
 *
 * Text inputs only, so a form that opens with a picker above its first field still puts the keyboard on the field.
 * Every component wants this same rule, which is why none of them names a field.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstTextInput(): FocusRequest<Id>? =
    elements.firstOrNull { it.id.isTextInput }?.let { FocusRequest(id = it.id) }

/**
 * Returns the first text input after [id], or null if [id] is the last one or is not part of the form. Fields that are
 * not text inputs are skipped, since focus is being moved on behalf of a keyboard.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.nextTextInputAfter(id: Id): Id? {
    val index = elements.indexOfFirst { it.id == id }
    if (index == -1) return null
    return elements.subList(index + 1, elements.size).firstOrNull { it.id.isTextInput }?.id
}

/**
 * Returns the keyboard action [id] should show. Only the last text input closes the keyboard, everything before it
 * moves on to the next field.
 *
 * This is only meaningful for a text input, as it is the only kind of field that has a keyboard.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.keyboardActionFor(id: Id): KeyboardAction {
    val lastTextInputId = elements.lastOrNull { it.id.isTextInput }?.id
    return if (id == lastTextInputId) KeyboardAction.DONE else KeyboardAction.NEXT
}

/**
 * The focus request to make when the shopper presses pay on a form that cannot be submitted, or null when every element
 * is valid.
 *
 * It carries `keepErrorHighlight`, because the point of this move is to show the shopper the error rather than to let
 * them start typing.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstInvalid(): FocusRequest<Id>? {
    val firstInvalidElement = elements.firstOrNull { !it.isValid }
    return firstInvalidElement?.let { FocusRequest(id = it.id, keepErrorHighlight = true) }
}
