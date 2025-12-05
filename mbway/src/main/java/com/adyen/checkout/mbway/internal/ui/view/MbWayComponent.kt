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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState
import com.adyen.checkout.mbway.internal.ui.state.MBWayChangeListener
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.ui.internal.element.ComponentScaffold
import com.adyen.checkout.ui.internal.element.button.PayButton
import com.adyen.checkout.ui.internal.element.input.CheckoutTextField
import com.adyen.checkout.ui.internal.element.input.DigitOnlyInputTransformation
import com.adyen.checkout.ui.internal.element.input.ValuePickerField
import com.adyen.checkout.ui.internal.theme.Dimensions

@Composable
internal fun MbWayComponent(
    viewState: MBWayViewState,
    changeListener: MBWayChangeListener,
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
        onSubmitClick = {},
        onCountryCodePickerClick = {},
    )
}
