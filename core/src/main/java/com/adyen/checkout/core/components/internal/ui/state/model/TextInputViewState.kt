/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/9/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormElementId
import com.adyen.checkout.core.components.internal.ui.state.form.FormState
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction
import com.adyen.checkout.core.components.internal.ui.state.form.keyboardActionFor
import com.adyen.checkout.ui.internal.element.input.FocusRequestToken

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class TextInputViewState(
    // Because of how our CheckoutTextField composable works, this field is only used as initial value, but will update
    // with the latest value of the input field.
    val text: String = "",
    val supportingText: CheckoutLocalizationKey? = null,
    val isError: Boolean = false,
    // The field specific icon, shown while the field is not in an error state. Do not render this directly, use
    // [trailingIcon] instead.
    val customTrailingIcon: TrailingIcon? = null,
    val isOptional: Boolean = false,
    // Only the last text input of a form closes the keyboard, so this is the form's answer, not the field's.
    val keyboardAction: KeyboardAction = KeyboardAction.DONE,
    // Set while the form is asking this field to take focus. The field reports back once it has.
    val focusRequest: FocusRequestToken? = null,
) {

    /**
     * The icon to render. Render this, not [customTrailingIcon]: the error icon wins, so every field shows an error
     * the same way.
     */
    val trailingIcon: TrailingIcon = if (isError) TrailingIcon.Error else customTrailingIcon ?: TrailingIcon.Empty
}

/**
 * Maps a field onto what the UI renders for it. Pass the whole [form] and [focusRequest]: this needs them to work out
 * the action key, which depends on what follows the field, and whether the field is the one being asked to take focus.
 *
 * Nothing here decides whether the field is shown. A hidden field is not one of its form's elements, so nothing asks
 * for its view state.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> TextInputComponentState.toViewState(
    form: FormState<Id>,
    focusRequest: FocusRequest<Id>?,
    id: Id,
    customTrailingIcon: TrailingIcon? = null,
): TextInputViewState {
    val isError = isErrorVisible
    return TextInputViewState(
        text = text,
        supportingText = if (isError) error?.message else description,
        isError = isError,
        customTrailingIcon = customTrailingIcon,
        isOptional = requirementPolicy is RequirementPolicy.Optional,
        keyboardAction = form.keyboardActionFor(id),
        focusRequest = focusRequest?.takeIf { it.id == id }?.let { FocusRequestToken(it) },
    )
}

@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@VisibleForTesting
val TextInputComponentState.isErrorVisible: Boolean
    get() = error?.isVisible == true
