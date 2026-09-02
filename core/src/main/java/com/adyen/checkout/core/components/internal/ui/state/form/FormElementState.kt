/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * One element of a form, as the form layer sees it.
 *
 * This deliberately holds facts rather than the element's own state. A component owns its fields and is the only thing
 * that can change them, so copying them in here would mean the same value could be read by two routes. Everything the
 * form's own rules need is derived instead, and anything else is asked of the component.
 *
 * Add a fact here when a rule in this package needs it and only the component can answer it. Every fact costs a line in
 * every component's builder, and is a value that can drift from the field it was derived from.
 *
 * @param id Which element this is.
 * @param isValid Whether the element currently holds a valid value, so that the form can find the first one that does
 * not. Elements that cannot be invalid, such as a switch, pass true.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FormElementState<Id : FormElementId>(
    val id: Id,
    val isValid: Boolean,
)

/**
 * This field as the form sees it, or null when the field is not on screen.
 *
 * Being absent is how the form represents a hidden field, so this is what a component's builder uses for a text input:
 * visibility and validity are then read off the same value and cannot disagree.
 *
 * Note that [TextInputComponentState.isValid] reflects the last time the validator ran, which is after the reducer that
 * is usually building this. That is accurate for a reduction that changes no text, which is the case for every rule
 * that asks about validity today.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> TextInputComponentState.toFormElementIfVisible(id: Id): FormElementState<Id>? =
    if (isVisible) FormElementState(id, isValid) else null
