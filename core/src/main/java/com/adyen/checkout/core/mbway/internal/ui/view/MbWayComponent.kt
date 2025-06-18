/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.internal.ui.model.CountryModel
import com.adyen.checkout.core.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.internal.ui.state.model.ViewFieldState
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.ui.internal.AdyenTextField

@Composable
internal fun MbWayComponent(
    viewState: MBWayViewState,
    fieldChangeListener: FieldChangeListener<MBWayFieldId>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // CountryCode
        val supportingTextCountryCode = if (viewState.countryCodeFieldState.errorMessageId != null) {
            "The country code is invalid"
        } else {
            null
        }
        AdyenTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.COUNTRY_CODE, focusState.hasFocus)
                },
            label = "Country Code",
            isError = viewState.countryCodeFieldState.errorMessageId != null,
            supportingText = supportingTextCountryCode,
            value = viewState.countryCodeFieldState.value.toShortString(),
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.COUNTRY_CODE, value)
            },
        )

        // PhoneNumber
        val supportingTextPhoneNumber = if (viewState.phoneNumberFieldState.errorMessageId != null) {
            "The phone number is invalid"
        } else {
            null
        }
        AdyenTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.PHONE_NUMBER, focusState.hasFocus)
                },
            label = "Phone Number",
            isError = viewState.phoneNumberFieldState.errorMessageId != null,
            supportingText = supportingTextPhoneNumber,
            value = viewState.phoneNumberFieldState.value,
            prefix = viewState.countryCodeFieldState.value.toShortString(),
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.PHONE_NUMBER, value)
            },
        )
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
            countryCodeFieldState = ViewFieldState(countries.first(), false),
            phoneNumberFieldState = ViewFieldState("", false),
        ),
        fieldChangeListener = object : FieldChangeListener<MBWayFieldId> {
            override fun <T> onFieldValueChanged(fieldId: MBWayFieldId, value: T) = Unit

            override fun onFieldFocusChanged(fieldId: MBWayFieldId, hasFocus: Boolean) = Unit
        },
    )
}
