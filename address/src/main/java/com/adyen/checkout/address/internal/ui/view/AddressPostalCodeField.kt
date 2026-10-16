/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * @param maxLength The selected country's maximum, never more than ten.
 */
@Composable
internal fun AddressPostalCodeField(
    label: CheckoutLocalizationKey,
    maxLength: Int,
    postalCodeState: TextInputViewState,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputTransformation = remember(maxLength) { InputTransformation.maxLength(maxLength) }

    AddressTextField(
        label = label,
        textInputViewState = postalCodeState,
        contentType = ContentType.PostalCode,
        inputTransformation = inputTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Unspecified,
        ),
        onValueChange = onValueChange,
        onFocusChange = onFocusChange,
        onFocusRequestConsumed = onFocusRequestConsumed,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun AddressPostalCodeFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressPostalCodeField(
            label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL,
            maxLength = PREVIEW_MAX_LENGTH,
            postalCodeState = TextInputViewState(text = "1011 DJ"),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )

        AddressPostalCodeField(
            label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL_ZIP_CODE,
            maxLength = PREVIEW_MAX_LENGTH,
            postalCodeState = TextInputViewState(
                text = "1234",
                supportingText = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR,
                isError = true,
            ),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )
    }
}

private const val PREVIEW_MAX_LENGTH = 10
