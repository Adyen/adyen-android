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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PayButton
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun MBWayContent(
    viewState: MBWayViewState,
    onIntent: (MBWayIntent) -> Unit,
    onSubmitClick: () -> Unit,
    onCountryCodePickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ComponentScaffold(
        modifier = modifier,
        disableInteraction = viewState.isLoading,
        footer = {
            PayButton(onClick = onSubmitClick, isLoading = viewState.isLoading)
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        ) {
            // CountryCode
            val country = viewState.selectedCountryCode
            ValuePickerField(
                value = "${country.callingCode} • ${country.countryName}",
                label = resolveString(CheckoutLocalizationKey.MBWAY_COUNTRY_CODE),
                onClick = onCountryCodePickerClick,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            if (viewState.phoneNumber != null) {
                // PhoneNumber
                MBWayPhoneNumberField(
                    mbWayPhoneNumberFieldState = viewState.phoneNumber,
                    countryCode = country.callingCode,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MBWayContentPreview() {
    val countries = listOf(
        CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
        CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
    )
    MBWayContent(
        viewState = MBWayViewState(
            countries = countries,
            isLoading = false,
            selectedCountryCode = countries.first(),
            phoneNumber = TextInputViewState(),
        ),
        onIntent = {},
        onSubmitClick = {},
        onCountryCodePickerClick = {},
    )
}
