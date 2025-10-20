/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2025.
 */

package com.adyen.checkout.mbway.internal.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.BodyEmphasized
import com.adyen.checkout.ui.internal.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.SubHeadline

@Suppress("LongMethod")
@Composable
internal fun CountryCodePicker(
    onDismissRequest: () -> Unit,
    viewState: MBWayViewState,
    onCountrySelected: (CountryModel) -> Unit,
) {
    Surface(
        color = CheckoutThemeProvider.colors.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.ExtraSmall),
            modifier = Modifier
                .systemBarsPadding()
                .padding(Dimensions.Large),
        ) {
            viewState.countries.forEach { country ->
                val isSelected = country == viewState.countryCode
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(CheckoutThemeProvider.attributes.cornerRadius.dp))
                        .let {
                            if (isSelected) {
                                it.background(CheckoutThemeProvider.colors.container)
                            } else {
                                it
                            }
                        }
                        .clickable(
                            interactionSource = null,
                            indication = ripple(color = CheckoutThemeProvider.colors.text),
                        ) {
                            onCountrySelected(country)
                            onDismissRequest()
                        }
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Column {
                        BodyEmphasized(country.callingCode)
                        SubHeadline(
                            text = "${country.isoCode} • ${country.countryName}",
                            color = CheckoutThemeProvider.colors.textSecondary,
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                R.drawable.ic_checkmark,
                            ),
                            contentDescription = null,
                            tint = CheckoutThemeProvider.colors.text,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CountryCodePickerPreview() {
    val countries = listOf(
        CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
        CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
    )
    CountryCodePicker(
        onDismissRequest = {},
        viewState = MBWayViewState(
            countries = countries,
            isLoading = false,
            countryCode = countries.first(),
            phoneNumber = TextInputState(),
        ),
        onCountrySelected = {},
    )
}
