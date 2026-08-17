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
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction

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
) {

    /**
     * The trailing icon to render. The generic [TrailingIcon.Error] icon always takes precedence over
     * [customTrailingIcon], so that every field shows the error state consistently.
     */
    val trailingIcon: TrailingIcon
        get() = if (isError) TrailingIcon.Error else customTrailingIcon ?: TrailingIcon.Empty
}

/**
 * Maps a TextInputComponentState to a TextInputViewState or returns null if the view should not be displayed on the UI.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.toViewState(
    customTrailingIcon: TrailingIcon? = null,
    // TODO - Form fields rollout: the default will be removed, so that every producer states the action its own form
    // decided on. DONE until then, because every component still without a form has a single text field, and that
    // field closes the keyboard.
    keyboardAction: KeyboardAction = KeyboardAction.DONE,
): TextInputViewState? {
    if (!isVisible) return null
    return TextInputViewState(
        text = text,
        supportingText = if (isErrorVisible) error?.message else description,
        isFocused = isFocused,
        isError = isErrorVisible,
        customTrailingIcon = customTrailingIcon,
        isOptional = requirementPolicy is RequirementPolicy.Optional,
        keyboardAction = keyboardAction,
    )
}
