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
 * The request that opens a form on the first field the shopper can type in, or null when it has none. Text inputs only,
 * so a form that opens with a picker above its first field still puts the keyboard on the field.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstTextInput(): FocusRequest<Id>? =
    elements.firstOrNull { it.id.isTextInput }?.let { FocusRequest(id = it.id) }

/**
 * The first text input after [id], or null if [id] is the last one or is not part of the form. Anything without a
 * keyboard is skipped, since this moves focus on a keyboard's behalf.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.nextTextInputAfter(id: Id): Id? {
    val index = elements.indexOfFirst { it.id == id }
    if (index == -1) return null
    return elements.subList(index + 1, elements.size).firstOrNull { it.id.isTextInput }?.id
}

/**
 * The keyboard action [id] should show. Only the last text input closes the keyboard; everything before it moves on.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.keyboardActionFor(id: Id): KeyboardAction {
    val lastTextInputId = elements.lastOrNull { it.id.isTextInput }?.id
    return if (id == lastTextInputId) KeyboardAction.DONE else KeyboardAction.NEXT
}

/**
 * The focus request to make when the shopper presses pay on a form that cannot be submitted, or null when every element
 * is valid. It keeps the error highlight, because the point of the move is to show the shopper what is wrong.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FormState<Id>.requestFocusOnFirstInvalid(): FocusRequest<Id>? {
    val firstInvalidElement = elements.firstOrNull { !it.isValid }
    return firstInvalidElement?.let { FocusRequest(id = it.id, keepErrorHighlight = true) }
}
