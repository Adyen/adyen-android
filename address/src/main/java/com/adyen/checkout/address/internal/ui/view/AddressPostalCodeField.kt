/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.address.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.internal.ui.CheckoutTextFieldTrailingIcon
import com.adyen.checkout.core.common.internal.ui.toImeAction
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.rememberTextFieldStateWithCurrentValue
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun AddressPostalCodeField(
    postalCodeState: TextInputViewState,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val supportingText = postalCodeState.supportingText?.let { resolveString(it) }

    CheckoutTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            },
        label = resolveString(CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL),
        state = rememberTextFieldStateWithCurrentValue(postalCodeState.text),
        contentType = ContentType.PostalCode,
        inputTransformation = InputTransformation.maxLength(PostalCodeProperties.POSTAL_CODE_MAX_LENGTH),
        isError = postalCodeState.isError,
        supportingText = supportingText,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Unspecified,
        ),
        isFocusRequested = postalCodeState.isFocusRequested,
        onFocusRequestConsumed = onFocusRequestConsumed,
        imeAction = postalCodeState.keyboardAction.toImeAction(),
        trailingIcon = {
            CheckoutTextFieldTrailingIcon(postalCodeState.trailingIcon)
        },
    )
}

@Preview
@Composable
private fun AddressPostalCodeFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressPostalCodeField(
            postalCodeState = TextInputViewState(text = "1234 AB"),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )

        AddressPostalCodeField(
            postalCodeState = TextInputViewState(
                text = "",
                supportingText = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR,
                isError = true,
            ),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )
    }
}
