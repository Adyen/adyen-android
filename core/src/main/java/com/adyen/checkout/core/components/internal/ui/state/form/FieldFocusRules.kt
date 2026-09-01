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
 * Returns this field as it should be after gaining or losing focus, which is a question of whether the shopper is now
 * shown the error it is holding.
 *
 * Every component decides this the same way, so the rule lives here. Applying it to the right field does not, because
 * only a component knows which property a field id names.
 *
 * Both outcomes do nothing to a field that has no error.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> TextInputComponentState.applyFocusChange(
    form: FormState<Id>,
    id: Id,
    hasFocus: Boolean,
): TextInputComponentState = when {
    // Whatever moved focus away, the shopper has finished with the field and can be told what is wrong with it.
    !hasFocus -> showErrorIfPresent()

    // Focus that follows pay exists to point at the error, so it shows it rather than clearing it. Highlighting has
    // already made the error visible by this point, so this only has to avoid undoing that.
    form.focusRequest?.takeIf { it.id == id }?.keepErrorHighlight == true -> showErrorIfPresent()

    // The shopper is moving into the field, either by tapping it or because a prefill sent them there. They should not
    // be greeted by an error about what they are about to change.
    else -> hideErrorIfPresent()
}

/**
 * Whether this focus change is the one a pending focus request asked for.
 *
 * A request that has been answered must be cleared, or it would outlive the focus change it asked for and make the
 * shopper's next tap on the same field look programmatic.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> FormState<Id>.answersFocusRequest(id: Id, hasFocus: Boolean): Boolean =
    hasFocus && focusRequest?.id == id

/**
 * The focus request to make when the shopper presses pay on a form that cannot be submitted, or null when every field
 * is valid.
 *
 * It carries `keepErrorHighlight`, because the point of this move is to show the shopper the error rather than to let
 * them start typing.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> FormState<Id>.requestFocusOnFirstInvalid(isValid: (Id) -> Boolean): FocusRequest<Id>? =
    firstInvalid(isValid)?.let { FocusRequest(id = it, keepErrorHighlight = true) }
