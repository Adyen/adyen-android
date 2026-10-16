/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun AddressHouseNumberField(
    label: CheckoutLocalizationKey,
    houseNumberState: TextInputViewState,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onFocusRequestConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AddressTextField(
        label = label,
        textInputViewState = houseNumberState,
        contentType = ContentType.AddressAuxiliaryDetails,
        inputTransformation = AddressLineInputTransformation,
        keyboardOptions = AddressLineKeyboardOptions,
        onValueChange = onValueChange,
        onFocusChange = onFocusChange,
        onFocusRequestConsumed = onFocusRequestConsumed,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun AddressHouseNumberFieldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressHouseNumberField(
            label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
            houseNumberState = TextInputViewState(
                supportingText = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_DESCRIPTION,
                isOptional = true,
            ),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )

        AddressHouseNumberField(
            label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL,
            houseNumberState = TextInputViewState(
                supportingText = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_ERROR,
                isError = true,
            ),
            onValueChange = {},
            onFocusChange = {},
            onFocusRequestConsumed = {},
        )
    }
}
