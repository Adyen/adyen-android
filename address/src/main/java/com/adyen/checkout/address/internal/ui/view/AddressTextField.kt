/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.address.internal.ui.properties.AddressLineProperties
import com.adyen.checkout.core.common.internal.ui.CheckoutTextFieldTrailingIcon
import com.adyen.checkout.core.common.internal.ui.toImeAction
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue

/** The street, the house number and the city: line breaks become spaces, and the length is capped. */
internal val AddressLineInputTransformation: InputTransformation = LineBreakToSpaceInputTransformation.then(
    InputTransformation.maxLength(AddressLineProperties.ADDRESS_LINE_MAX_LENGTH),
)

internal val AddressLineKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Text,
    capitalization = KeyboardCapitalization.Sentences,
)

/**
 * A text field of the address form. The label is the selected country's, and an optional field appends
 * [CheckoutLocalizationKey.GENERAL_OPTIONAL] to it, as card's fields do.
 */
@Composable
internal fun AddressTextField(
    label: CheckoutLocalizationKey,
    textInputViewState: TextInputViewState,
    contentType: ContentType,
    inputTransformation: InputTransformation,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelSuffix = if (textInputViewState.isOptional) {
        " ${resolveString(CheckoutLocalizationKey.GENERAL_OPTIONAL)}"
    } else {
        ""
    }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        label = resolveString(label) + labelSuffix,
        state = rememberTextFieldStateWithCurrentValue(textInputViewState.text),
        contentType = contentType,
        isError = textInputViewState.isError,
        supportingText = textInputViewState.supportingText?.let { resolveString(it) },
        onValueChange = onValueChange,
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions,
        isFocusRequested = textInputViewState.isFocusRequested,
        onFocusRequestConsumed = onFocusRequestConsumed,
        imeAction = textInputViewState.keyboardAction.toImeAction(),
        trailingContent = {
            CheckoutTextFieldTrailingIcon(textInputViewState.trailingIcon)
        },
    )
}
