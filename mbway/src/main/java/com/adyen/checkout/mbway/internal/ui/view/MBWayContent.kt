/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.mbway.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.payButtonAsComponentScaffoldFooter
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.mbway.internal.ui.state.CountryPickerViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayFormElement
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun MBWayContent(
    modifier: Modifier,
    viewStateFlow: StateFlow<MBWayViewState>,
    onIntent: (MBWayIntent) -> Unit,
    onSubmitClick: () -> Unit,
    onCountryCodePickerClick: () -> Unit,
) {
    val viewState by viewStateFlow.collectAsStateWithLifecycle()

    MBWayContent(
        modifier = modifier,
        viewState = viewState,
        onIntent = onIntent,
        onSubmitClick = onSubmitClick,
        onCountryCodePickerClick = onCountryCodePickerClick,
    )
}

@Composable
private fun MBWayContent(
    viewState: MBWayViewState,
    onIntent: (MBWayIntent) -> Unit,
    onSubmitClick: () -> Unit,
    onCountryCodePickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = payButtonAsComponentScaffoldFooter(viewState.payButtonViewState, onSubmitClick),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        ) {
            // Keyed on the id rather than the position, so a field's text and focus follow it when the order changes.
            viewState.elements.forEach { element ->
                key(element.id) {
                    MBWayFormElementContent(
                        element = element,
                        onIntent = onIntent,
                        onCountryCodePickerClick = onCountryCodePickerClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun MBWayFormElementContent(
    element: MBWayFormElement,
    onIntent: (MBWayIntent) -> Unit,
    onCountryCodePickerClick: () -> Unit,
) {
    when (element) {
        is MBWayFormElement.CountryCode -> ValuePickerField(
            value = "${element.selectedCountry.callingCode} • ${element.selectedCountry.countryName}",
            label = resolveString(CheckoutLocalizationKey.MBWAY_COUNTRY_CODE),
            onClick = onCountryCodePickerClick,
            modifier = Modifier.fillMaxWidth(),
        )

        is MBWayFormElement.PhoneNumber -> MBWayPhoneNumberField(
            mbWayPhoneNumberFieldState = element.textInputViewState,
            countryCode = element.callingCode,
            onValueChange = { onIntent(MBWayIntent.UpdatePhoneNumber(it)) },
            onFocusChange = { onIntent(MBWayIntent.UpdateFieldFocus(element.id, it)) },
            onFocusRequestConsumed = { onIntent(MBWayIntent.FocusRequestConsumed(element.id)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MBWayContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val countries = listOf(
            CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
            CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
        )
        MBWayContent(
            viewState = MBWayViewState(
                elements = listOf(
                    MBWayFormElement.CountryCode(selectedCountry = countries.first()),
                    MBWayFormElement.PhoneNumber(TextInputViewState(), callingCode = countries.first().callingCode),
                ),
                isLoading = false,
                payButtonViewState = PayButtonViewState(null, false),
                countryPickerViewState = CountryPickerViewState(countries, countries.first()),
            ),
            onIntent = {},
            onSubmitClick = {},
            onCountryCodePickerClick = {},
        )
    }
}
