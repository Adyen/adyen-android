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
    // TODO - Form fields cleanup: will be removed. FormState.focusRequest does this job instead. It stays until every
    // component has moved over to it.
    val isFocused: Boolean = false,
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
 * This says nothing about whether the field is shown. A producer that decides visibility per field, rather than from a
 * form's field order, wants [toViewStateIfVisible] instead.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.toViewState(
    customTrailingIcon: TrailingIcon? = null,
    // TODO - Form fields rollout: the default will be removed, so that every producer states the action its own form
    // decided on. DONE until then, because every component still without a form has a single text field, and that
    // field closes the keyboard.
    keyboardAction: KeyboardAction = KeyboardAction.DONE,
    focusRequest: FocusRequestToken? = null,
): TextInputViewState = TextInputViewState(
    text = text,
    supportingText = if (isErrorVisible) error?.message else description,
    isFocused = isFocused,
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

/**
 * Maps a text input onto what the UI renders for it, or null if the field is not shown.
 *
 * A component whose form publishes an ordered list of the fields it shows does not need this: a field that is not shown
 * is not in the list, so nothing asks for its view state in the first place.
 */
// TODO - POC: remove once every component publishes an element list
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.toViewStateIfVisible(
    customTrailingIcon: TrailingIcon? = null,
    keyboardAction: KeyboardAction = KeyboardAction.DONE,
    focusRequest: FocusRequestToken? = null,
): TextInputViewState? {
    if (!isVisible) return null
    return toViewState(
        customTrailingIcon = customTrailingIcon,
        keyboardAction = keyboardAction,
        focusRequest = focusRequest,
    )
}
