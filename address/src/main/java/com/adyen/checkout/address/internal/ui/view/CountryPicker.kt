/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.address.internal.ui.state.CountryPickerViewState
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.ui.internal.element.SearchableValuePicker
import com.adyen.checkout.ui.internal.element.ValuePickerItem
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun CountryPicker(
    viewState: CountryPickerViewState,
    onItemClick: (CountryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countries = remember(viewState) {
        viewState.countries.map {
            ValuePickerItem(
                id = it.isoCode,
                title = it.countryName,
                subtitle = it.isoCode,
                isSelected = it == viewState.selectedCountry,
            )
        }
    }
    SearchableValuePicker(
        searchHint = resolveString(CheckoutLocalizationKey.GENERAL_SEARCH_HINT),
        items = countries,
        onItemClick = { item ->
            viewState.countries.find { it.isoCode == item.id }?.let(onItemClick)
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CountryPickerPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val countries = listOf(
            CountryModel(isoCode = "NL", countryName = "Netherlands", callingCode = "+31"),
            CountryModel(isoCode = "US", countryName = "United States", callingCode = "+1"),
        )
        CountryPicker(
            viewState = CountryPickerViewState(
                countries = countries,
                selectedCountry = countries.first(),
            ),
            onItemClick = {},
        )
    }
}
