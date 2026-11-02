/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 30/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.state.PickerViewState
import com.adyen.checkout.core.common.internal.ui.ScreenHeader
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.SearchableValuePicker
import com.adyen.checkout.ui.internal.element.ValuePickerItem
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * The country or the state picker screen, titled with the label of the field that opens it. Empty while the regions
 * load or after they failed to load.
 *
 * @param onItemClick Receives the code of the picked region.
 */
@Composable
internal fun AddressRegionPicker(
    viewState: PickerViewState,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(viewState) { viewState.toValuePickerItems() }

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        modifier = modifier,
    ) {
        ScreenHeader(title = resolveString(viewState.title))

        SearchableValuePicker(
            searchHint = resolveString(CheckoutLocalizationKey.GENERAL_SEARCH_HINT),
            items = items,
            onItemClick = { onItemClick(it.id) },
        )
    }
}

/**
 * Matches the selection by code, since a selection made before its regions load is not in [PickerViewState.regions].
 */
internal fun PickerViewState.toValuePickerItems(): List<ValuePickerItem> = regions.map { region ->
    ValuePickerItem(
        id = region.code,
        title = region.name,
        subtitle = region.code,
        isSelected = region.code == selected?.code,
    )
}

@Preview(showBackground = true)
@Composable
private fun AddressRegionPickerCountryPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val countries = listOf(
            AddressRegion(code = "NL", name = "Netherlands"),
            AddressRegion(code = "GB", name = "United Kingdom"),
            AddressRegion(code = "US", name = "United States"),
        )
        AddressRegionPicker(
            viewState = PickerViewState(
                title = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
                regions = countries,
                selected = countries.first(),
            ),
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressRegionPickerStatePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        val states = listOf(
            AddressRegion(code = "AB", name = "Alberta"),
            AddressRegion(code = "BC", name = "British Columbia"),
            AddressRegion(code = "ON", name = "Ontario"),
        )
        AddressRegionPicker(
            viewState = PickerViewState(
                title = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_PROVINCE_OR_TERRITORY,
                regions = states,
                selected = null,
            ),
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressRegionPickerEmptyPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AddressRegionPicker(
            viewState = PickerViewState(
                title = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_LABEL_STATE,
                regions = emptyList(),
                selected = null,
            ),
            onItemClick = {},
        )
    }
}
