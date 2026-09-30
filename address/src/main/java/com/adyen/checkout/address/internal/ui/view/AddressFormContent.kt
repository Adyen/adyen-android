/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.address.internal.ui.state.AddressFormElement
import com.adyen.checkout.address.internal.ui.state.AddressIntent
import com.adyen.checkout.address.internal.ui.state.AddressViewState
import com.adyen.checkout.address.internal.ui.state.CountryPickerViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun AddressFormContent(
    viewState: AddressViewState,
    onIntent: (AddressIntent) -> Unit,
    onCountryPickerClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        modifier = modifier,
    ) {
        Title(resolveString(CheckoutLocalizationKey.ADDRESS_TITLE))

        // Keyed on the id rather than the position, so a field's text and focus follow it when the order changes.
        viewState.elements.forEach { element ->
            key(element.id) {
                AddressFormElementContent(
                    element = element,
                    onIntent = onIntent,
                    onCountryPickerClick = onCountryPickerClick,
                )
            }
        }

        PrimaryButton(
            onClick = onConfirmClick,
            text = resolveString(CheckoutLocalizationKey.GENERAL_CONFIRM_LABEL),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AddressFormElementContent(
    element: AddressFormElement,
    onIntent: (AddressIntent) -> Unit,
    onCountryPickerClick: () -> Unit,
) {
    when (element) {
        is AddressFormElement.Country -> ValuePickerField(
            value = element.selectedCountry?.countryName.orEmpty(),
            label = resolveString(CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL),
            onClick = onCountryPickerClick,
            supportingText = element.errorMessage?.let { resolveString(it) },
            isError = element.errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )

        is AddressFormElement.PostalCode -> AddressPostalCodeField(
            postalCodeState = element.textInputViewState,
            onValueChange = { onIntent(AddressIntent.UpdatePostalCode(it)) },
            onFocusChange = { onIntent(AddressIntent.UpdateFieldFocus(element.id, it)) },
            onFocusRequestConsumed = { onIntent(AddressIntent.FocusRequestConsumed(element.id)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressFormContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressFormContent(
            viewState = AddressViewState(
                elements = listOf(
                    AddressFormElement.Country(selectedCountry = NETHERLANDS, errorMessage = null),
                    AddressFormElement.PostalCode(TextInputViewState(text = "1234 AB")),
                ),
                countryPickerViewState = CountryPickerViewState(listOf(NETHERLANDS), NETHERLANDS),
            ),
            onIntent = {},
            onCountryPickerClick = {},
            onConfirmClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressFormContentErrorPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressFormContent(
            viewState = AddressViewState(
                elements = listOf(
                    AddressFormElement.Country(
                        selectedCountry = null,
                        errorMessage = CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR,
                    ),
                    AddressFormElement.PostalCode(
                        TextInputViewState(
                            supportingText = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR,
                            isError = true,
                        ),
                    ),
                ),
                countryPickerViewState = CountryPickerViewState(listOf(NETHERLANDS), null),
            ),
            onIntent = {},
            onCountryPickerClick = {},
            onConfirmClick = {},
        )
    }
}

private val NETHERLANDS = CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31")
