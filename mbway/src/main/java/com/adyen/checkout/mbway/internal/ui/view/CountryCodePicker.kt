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
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.ui.internal.element.SearchableValuePicker
import com.adyen.checkout.ui.internal.element.ValuePickerItem

@Composable
internal fun CountryCodePicker(
    viewState: MBWayViewState,
    onItemClick: (CountryModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countries = remember(viewState.countries) {
        viewState.countries.map {
            ValuePickerItem(
                id = it.isoCode,
                title = it.callingCode,
                subtitle = "${it.isoCode} • ${it.countryName}",
                isSelected = it == viewState.countryCode,
            )
        }
    }
    SearchableValuePicker(
        searchHint = resolveString(CheckoutLocalizationKey.GENERAL_SEARCH_HINT),
        items = countries,
        onItemClick = { item ->
            val country = viewState.countries.find { it.isoCode == item.id } ?: viewState.countryCode
            onItemClick(country)
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun CountryCodePickerPreview() {
    val countries = listOf(
        CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
        CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
    )
    CountryCodePicker(
        viewState = MBWayViewState(
            countries = countries,
            isLoading = false,
            countryCode = countries.first(),
            phoneNumber = TextInputViewState(),
        ),
        onItemClick = {},
    )
}
