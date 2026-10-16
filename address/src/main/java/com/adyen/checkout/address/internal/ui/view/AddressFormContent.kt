/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
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
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.state.AddressFormElement
import com.adyen.checkout.address.internal.ui.state.AddressIntent
import com.adyen.checkout.address.internal.ui.state.AddressViewState
import com.adyen.checkout.address.internal.ui.state.PickerViewState
import com.adyen.checkout.core.common.internal.ui.ScreenHeader
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * The address form screen: a header, one row per element in the selected country's order, and the save button as the
 * footer, as the payment components place their pay button.
 */
@Composable
internal fun AddressFormContent(
    viewState: AddressViewState,
    onIntent: (AddressIntent) -> Unit,
    onCountryClick: () -> Unit,
    onStateOrProvinceClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = false,
        footer = {
            PrimaryButton(
                onClick = onSaveClick,
                text = resolveString(CheckoutLocalizationKey.ADDRESS_SAVE_LABEL),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        ) {
            ScreenHeader(
                title = resolveString(CheckoutLocalizationKey.ADDRESS_TITLE),
                subtitle = resolveString(CheckoutLocalizationKey.ADDRESS_DESCRIPTION),
            )

            AddressForm(
                elements = viewState.elements,
                onIntent = onIntent,
                onCountryClick = onCountryClick,
                onStateOrProvinceClick = onStateOrProvinceClick,
            )
        }
    }
}

@Composable
private fun AddressForm(
    elements: List<AddressFormElement>,
    onIntent: (AddressIntent) -> Unit,
    onCountryClick: () -> Unit,
    onStateOrProvinceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
    ) {
        // Keyed on the id rather than the position, so a field's text and focus follow it when the order changes.
        elements.forEach { element ->
            key(element.id) {
                AddressFormElementContent(
                    element = element,
                    onIntent = onIntent,
                    onCountryClick = onCountryClick,
                    onStateOrProvinceClick = onStateOrProvinceClick,
                )
            }
        }
    }
}

@Composable
private fun AddressFormElementContent(
    element: AddressFormElement,
    onIntent: (AddressIntent) -> Unit,
    onCountryClick: () -> Unit,
    onStateOrProvinceClick: () -> Unit,
) {
    val onFocusChange = { hasFocus: Boolean -> onIntent(AddressIntent.UpdateFieldFocus(element.id, hasFocus)) }
    val onFocusRequestConsumed = { onIntent(AddressIntent.FocusRequestConsumed(element.id)) }

    when (element) {
        is AddressFormElement.Country -> AddressPickerRow(
            label = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
            selected = element.selected,
            errorMessage = element.errorMessage,
            onClick = onCountryClick,
        )

        is AddressFormElement.Street -> AddressStreetField(
            label = element.label,
            streetState = element.textInputViewState,
            onValueChange = { onIntent(AddressIntent.UpdateStreet(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is AddressFormElement.HouseNumberOrName -> AddressHouseNumberField(
            label = element.label,
            houseNumberState = element.textInputViewState,
            onValueChange = { onIntent(AddressIntent.UpdateHouseNumberOrName(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is AddressFormElement.PostalCode -> AddressPostalCodeField(
            label = element.label,
            maxLength = element.maxLength,
            postalCodeState = element.textInputViewState,
            onValueChange = { onIntent(AddressIntent.UpdatePostalCode(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is AddressFormElement.City -> AddressCityField(
            label = element.label,
            cityState = element.textInputViewState,
            onValueChange = { onIntent(AddressIntent.UpdateCity(it)) },
            onFocusChange = onFocusChange,
            onFocusRequestConsumed = onFocusRequestConsumed,
        )

        is AddressFormElement.StateOrProvince -> AddressPickerRow(
            label = element.label,
            selected = element.selected,
            errorMessage = element.errorMessage,
            onClick = onStateOrProvinceClick,
        )
    }
}

/** A row that shows the picked country or state and opens its picker. Pickers cannot be autofilled. */
@Composable
private fun AddressPickerRow(
    label: CheckoutLocalizationKey,
    selected: AddressRegion?,
    errorMessage: CheckoutLocalizationKey?,
    onClick: () -> Unit,
) {
    ValuePickerField(
        value = selected?.name.orEmpty(),
        label = resolveString(label),
        onClick = onClick,
        supportingText = errorMessage?.let { resolveString(it) },
        isError = errorMessage != null,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun AddressFormContentUsPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressFormContent(
            viewState = previewViewState(
                AddressFormElement.Country(
                    selected = AddressRegion(code = "US", name = "United States"),
                    errorMessage = null,
                ),
                AddressFormElement.Street(
                    label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL_ADDRESS,
                    textInputViewState = TextInputViewState(text = "1 Market Street"),
                ),
                AddressFormElement.HouseNumberOrName(
                    label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL_APARTMENT_SUITE,
                    textInputViewState = TextInputViewState(
                        supportingText = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_DESCRIPTION,
                        isOptional = true,
                    ),
                ),
                AddressFormElement.City(
                    label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL,
                    textInputViewState = TextInputViewState(text = "San Francisco"),
                ),
                AddressFormElement.StateOrProvince(
                    label = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
                    selected = AddressRegion(code = "CA", name = "California"),
                    errorMessage = null,
                ),
                AddressFormElement.PostalCode(
                    label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL_ZIP_CODE,
                    maxLength = PREVIEW_POSTAL_CODE_MAX_LENGTH,
                    textInputViewState = TextInputViewState(text = "94105"),
                ),
            ),
            onIntent = {},
            onCountryClick = {},
            onStateOrProvinceClick = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressFormContentGbPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressFormContent(
            viewState = previewViewState(
                AddressFormElement.Country(
                    selected = AddressRegion(code = "GB", name = "United Kingdom"),
                    errorMessage = null,
                ),
                AddressFormElement.HouseNumberOrName(
                    label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL,
                    textInputViewState = TextInputViewState(
                        text = "10",
                        supportingText = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_DESCRIPTION,
                    ),
                ),
                AddressFormElement.Street(
                    label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL,
                    textInputViewState = TextInputViewState(text = "Downing Street"),
                ),
                AddressFormElement.City(
                    label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL_CITY_TOWN,
                    textInputViewState = TextInputViewState(text = "London"),
                ),
                AddressFormElement.PostalCode(
                    label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL,
                    maxLength = PREVIEW_POSTAL_CODE_MAX_LENGTH,
                    textInputViewState = TextInputViewState(text = "SW1A 2AA"),
                ),
            ),
            onIntent = {},
            onCountryClick = {},
            onStateOrProvinceClick = {},
            onSaveClick = {},
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
            viewState = previewViewState(
                AddressFormElement.Country(
                    selected = null,
                    errorMessage = CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR,
                ),
                AddressFormElement.Street(
                    label = CheckoutLocalizationKey.ADDRESS_STREET_LABEL,
                    textInputViewState = TextInputViewState(
                        supportingText = CheckoutLocalizationKey.ADDRESS_STREET_ERROR,
                        isError = true,
                    ),
                ),
                AddressFormElement.HouseNumberOrName(
                    label = CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_LABEL,
                    textInputViewState = TextInputViewState(
                        text = "6 🏠",
                        supportingText = CheckoutLocalizationKey.ADDRESS_INVALID_CHARACTERS_ERROR,
                        isError = true,
                    ),
                ),
                AddressFormElement.PostalCode(
                    label = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_LABEL,
                    maxLength = PREVIEW_POSTAL_CODE_MAX_LENGTH,
                    textInputViewState = TextInputViewState(
                        text = "1234",
                        supportingText = CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR,
                        isError = true,
                    ),
                ),
                AddressFormElement.City(
                    label = CheckoutLocalizationKey.ADDRESS_CITY_LABEL,
                    textInputViewState = TextInputViewState(
                        supportingText = CheckoutLocalizationKey.ADDRESS_CITY_ERROR,
                        isError = true,
                    ),
                ),
            ),
            onIntent = {},
            onCountryClick = {},
            onStateOrProvinceClick = {},
            onSaveClick = {},
        )
    }
}

private fun previewViewState(vararg elements: AddressFormElement) = AddressViewState(
    elements = elements.toList(),
    countryPickerViewState = PickerViewState(
        title = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
        regions = emptyList(),
        selected = null,
    ),
    statePickerViewState = null,
)

private const val PREVIEW_POSTAL_CODE_MAX_LENGTH = 10
