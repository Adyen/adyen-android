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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.components.internal.ui.state.model.ViewFieldState
import com.adyen.checkout.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.ui.internal.AdyenCheckoutTheme
import com.adyen.checkout.ui.internal.AdyenTextField
import com.adyen.checkout.ui.internal.BodyEmphasized
import com.adyen.checkout.ui.internal.FullScreenDialog
import com.adyen.checkout.ui.internal.SubHeadline
import com.adyen.checkout.ui.internal.ValuePickerField

// TODO - replace hardcoded strings with resources
@Composable
internal fun MbWayComponent(
    viewState: MBWayViewState,
    fieldChangeListener: FieldChangeListener<MBWayFieldId>,
    modifier: Modifier = Modifier,
) {
    var showCountryCodeDialog by remember { mutableStateOf(false) }
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
        val country = viewState.countryCodeFieldState.value
        ValuePickerField(
            value = "${country.callingCode} • ${country.countryName}",
            label = "Country Code",
            supportingText = supportingTextCountryCode,
            isError = viewState.countryCodeFieldState.errorMessageId != null,
            onClick = { showCountryCodeDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.COUNTRY_CODE, focusState.hasFocus)
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
            prefix = country.callingCode,
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.PHONE_NUMBER, value)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
    }

    // TODO - replace with actual navigation after it has been implemented
    if (showCountryCodeDialog) {
        CountryCodeDialog(
            onDismissRequest = { showCountryCodeDialog = false },
            viewState = viewState,
            fieldChangeListener = fieldChangeListener,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun CountryCodeDialog(
    onDismissRequest: () -> Unit,
    viewState: MBWayViewState,
    fieldChangeListener: FieldChangeListener<MBWayFieldId>,
) {
    FullScreenDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            viewState.countries.forEach { country ->
                val isSelected = country == viewState.countryCodeFieldState.value
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(AdyenCheckoutTheme.elements.cornerRadius.dp))
                        .let {
                            if (isSelected) {
                                it.background(AdyenCheckoutTheme.colors.container)
                            } else {
                                it
                            }
                        }
                        .clickable(
                            interactionSource = null,
                            indication = ripple(color = AdyenCheckoutTheme.colors.text),
                        ) {
                            fieldChangeListener.onFieldValueChanged(MBWayFieldId.COUNTRY_CODE, country)
                            onDismissRequest()
                        }
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Column {
                        BodyEmphasized(country.callingCode)
                        SubHeadline(
                            text = "${country.isoCode} • ${country.countryName}",
                            color = AdyenCheckoutTheme.colors.textSecondary,
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                com.adyen.checkout.test.R.drawable.ic_checkmark,
                            ),
                            contentDescription = null,
                            tint = AdyenCheckoutTheme.colors.text,
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
            countryCodeFieldState = ViewFieldState(countries.first(), false),
            phoneNumberFieldState = ViewFieldState("", false),
        ),
        fieldChangeListener = object : FieldChangeListener<MBWayFieldId> {
            override fun <T> onFieldValueChanged(fieldId: MBWayFieldId, value: T) = Unit

            override fun onFieldFocusChanged(fieldId: MBWayFieldId, hasFocus: Boolean) = Unit
        },
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
            countryCodeFieldState = ViewFieldState(countries.first(), false),
            phoneNumberFieldState = ViewFieldState("", false),
        ),
        fieldChangeListener = object : FieldChangeListener<MBWayFieldId> {
            override fun <T> onFieldValueChanged(fieldId: MBWayFieldId, value: T) = Unit

            override fun onFieldFocusChanged(fieldId: MBWayFieldId, hasFocus: Boolean) = Unit
        },
    )
}
