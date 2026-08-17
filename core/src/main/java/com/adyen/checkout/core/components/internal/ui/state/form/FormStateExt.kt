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
 * Returns the last text input of the form, or null if the form has none. This is the field that shows
 * [KeyboardAction.DONE].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> FormState<Id>.lastTextInput(): Id? = order.lastOrNull { it.isTextInput }

/**
 * Returns the first text input after [id], or null if [id] is the last one or is not part of the form. Fields that are
 * not text inputs are skipped, since focus is being moved on behalf of a keyboard.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> FormState<Id>.nextTextInputAfter(id: Id): Id? {
    val index = order.indexOf(id)
    if (index == -1) return null
    return order.subList(index + 1, order.size).firstOrNull { it.isTextInput }
}

/**
 * Returns the keyboard action [id] should show. Only the last text input closes the keyboard, everything before it
 * moves on to the next field.
 *
 * This is only meaningful for a text input, as it is the only kind of field that has a keyboard.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> FormState<Id>.keyboardActionFor(id: Id): KeyboardAction {
    return if (id == lastTextInput()) KeyboardAction.DONE else KeyboardAction.NEXT
}

/**
 * Returns the first invalid field in the order the shopper sees, or null if every field is valid. This is the field
 * that receives focus when the shopper presses pay on a form that cannot be submitted.
 *
 * Validity is passed in as a function so that the form does not need to know how a component stores its values. Any
 * field can be reported invalid, including one that is not a text input.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> FormState<Id>.firstInvalid(isValid: (Id) -> Boolean): Id? {
    return order.firstOrNull { !isValid(it) }
}
