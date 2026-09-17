/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2025.
 */

package com.adyen.checkout.mbway.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.mbway.internal.ui.state.CountryPickerViewState
import com.adyen.checkout.ui.internal.element.SearchableValuePicker
import com.adyen.checkout.ui.internal.element.ValuePickerItem
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun CountryCodePicker(
    viewState: CountryPickerViewState,
    onItemClick: (CountryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countries = remember(viewState) {
        viewState.countries.map {
            ValuePickerItem(
                id = it.isoCode,
                title = it.callingCode,
                subtitle = "${it.isoCode} • ${it.countryName}",
                isSelected = it == viewState.selectedCountry,
            )
        }
    }
    SearchableValuePicker(
        searchHint = resolveString(CheckoutLocalizationKey.GENERAL_SEARCH_HINT),
        items = countries,
        onItemClick = { item ->
            val country = viewState.countries.find { it.isoCode == item.id } ?: viewState.selectedCountry
            onItemClick(country)
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CountryCodePickerPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val countries = listOf(
            CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
            CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
        )
        CountryCodePicker(
            viewState = CountryPickerViewState(
                countries = countries,
                selectedCountry = countries.first(),
            ),
            onItemClick = {},
        )
    }
}
