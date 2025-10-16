/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.mbway.internal.ui.state.MBWayChangeListener
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.BodyEmphasized
import com.adyen.checkout.ui.internal.CheckoutTextField
import com.adyen.checkout.ui.internal.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.Dimensions
import com.adyen.checkout.ui.internal.SubHeadline
import com.adyen.checkout.ui.internal.ValuePickerField

// TODO - replace hardcoded strings with resources
@Composable
internal fun MbWayComponent(
    viewState: MBWayViewState,
    changeListener: MBWayChangeListener,
    onCountryCodePickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Large),
    ) {
        // CountryCode
        val country = viewState.countryCode
        ValuePickerField(
            value = "${country.callingCode} • ${country.countryName}",
            label = resolveString(CheckoutLocalizationKey.MBWAY_COUNTRY_CODE),
            onClick = onCountryCodePickerClick,
            modifier = Modifier
                .fillMaxWidth(),
        )

        // PhoneNumber
        val showPhoneNumberError = viewState.phoneNumber.errorMessage != null && viewState.phoneNumber.showError
        val supportingTextPhoneNumber = if (showPhoneNumberError) {
            viewState.phoneNumber.errorMessage?.let { resolveString(it) }
        } else {
            null
        }

        CheckoutTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    changeListener.onPhoneNumberFocusChanged(focusState.hasFocus)
                },
            label = resolveString(CheckoutLocalizationKey.MBWAY_PHONE_NUMBER),
            initialValue = viewState.phoneNumber.text,
            isError = showPhoneNumberError,
            supportingText = supportingTextPhoneNumber,
            prefix = country.callingCode,
            onValueChange = { value ->
                changeListener.onPhoneNumberChanged(value)
            },
            inputTransformation = DigitOnlyInputTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shouldFocus = viewState.phoneNumber.isFocused,
        )
    }
}

// TODO: Extract this method into a new file and separate dialog related logic (surface and system bar paddings)
@Suppress("LongMethod")
@Composable
internal fun CountryCodeDialog(
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

@Preview(showBackground = true)
@Composable
private fun MbWayComponentPreview() {
    val countries = listOf(
        CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
        CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
    )
    MbWayComponent(
        viewState = MBWayViewState(
            countries = countries,
            isLoading = false,
            countryCode = countries.first(),
            phoneNumber = TextInputState(),
        ),
        changeListener = object : MBWayChangeListener {
            override fun onCountryChanged(newCountryCode: CountryModel) = Unit
            override fun onPhoneNumberChanged(newPhoneNumber: String) = Unit
            override fun onPhoneNumberFocusChanged(hasFocus: Boolean) = Unit
        },
        onCountryCodePickerClick = {},
    )
}

@Preview
@Composable
private fun CountryCodeDialogPreview() {
    val countries = listOf(
        CountryModel(isoCode = "PT", countryName = "Portugal", callingCode = "+351"),
        CountryModel(isoCode = "ES", countryName = "Spain", callingCode = "+34"),
    )
    CountryCodeDialog(
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
