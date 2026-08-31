/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/9/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.FormFieldId
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
    // The action key this field shows on the keyboard. Only the last text input of a form closes the keyboard, so this
    // can only be derived from the form as a whole, through `FormState.keyboardActionFor`.
    val keyboardAction: KeyboardAction = KeyboardAction.DONE,
    // Set when the form is asking this field to take focus. The field reports back once it has acted on it.
    val focusRequest: FocusRequestToken? = null,
) {

    /**
     * The trailing icon to render. The generic [TrailingIcon.Error] icon always takes precedence over
     * [customTrailingIcon], so that every field shows the error state consistently.
     */
    val trailingIcon: TrailingIcon
        get() = if (isError) TrailingIcon.Error else customTrailingIcon ?: TrailingIcon.Empty
}

/**
 * Maps a text input onto what the UI renders for it.
 *
 * This says nothing about whether the field is shown: a field that is not shown is not in its form's order, so nothing
 * asks for its view state. Producers use the overload below, which reads the keyboard action and the focus request from
 * the form rather than being told them.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.toViewState(
    customTrailingIcon: TrailingIcon? = null,
    keyboardAction: KeyboardAction = KeyboardAction.DONE,
    focusRequest: FocusRequestToken? = null,
): TextInputViewState = TextInputViewState(
    text = text,
    supportingText = if (isErrorVisible) error?.message else description,
    isError = isErrorVisible,
    customTrailingIcon = customTrailingIcon,
    isOptional = requirementPolicy is RequirementPolicy.Optional,
    keyboardAction = keyboardAction,
    focusRequest = focusRequest,
)

/**
 * Maps a text input onto what the UI renders for it, taking from [form] the two things only the form as a whole can
 * answer: which action key the field shows, and whether it is the one being asked to take focus.
 *
 * The token the UI compares is built here rather than by the caller, because turning a form's [FocusRequest] into a
 * [FocusRequestToken] is this layer's plumbing and says nothing about any one component.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormFieldId> TextInputComponentState.toViewState(
    form: FormState<Id>,
    id: Id,
    customTrailingIcon: TrailingIcon? = null,
): TextInputViewState = toViewState(
    customTrailingIcon = customTrailingIcon,
    keyboardAction = form.keyboardActionFor(id),
    focusRequest = form.focusRequest?.takeIf { it.id == id }?.let { FocusRequestToken(it) },
)
