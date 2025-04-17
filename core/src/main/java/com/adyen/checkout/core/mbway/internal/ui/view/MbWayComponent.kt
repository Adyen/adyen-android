/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.adyen.checkout.core.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.ui.internal.AdyenCheckoutTheme

@Composable
internal fun MbWayComponent(
    viewState: MBWayViewState,
    fieldChangeListener: FieldChangeListener<MBWayFieldId>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // CountryCode
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.COUNTRY_CODE, focusState.hasFocus)
                },
            label = {
                Text("Country Code")
            },
            isError = viewState.countryCodeFieldState.errorMessageId != null,
            supportingText = {
                if (viewState.countryCodeFieldState.errorMessageId != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "The country code is invalid",
                        color = AdyenCheckoutTheme.colors.destructive,
                    )
                }
            },
            value = viewState.countryCodeFieldState.value,
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.COUNTRY_CODE, value)
            },
        )

        // PhoneNumber
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.PHONE_NUMBER, focusState.hasFocus)
                },
            label = {
                Text("Phone Number")
            },
            isError = viewState.phoneNumberFieldState.errorMessageId != null,
            supportingText = {
                if (viewState.phoneNumberFieldState.errorMessageId != null) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "The phone number is invalid",
                        color = AdyenCheckoutTheme.colors.destructive,
                    )
                }
            },
            value = viewState.phoneNumberFieldState.value,
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.PHONE_NUMBER, value)
            },
        )
    }
}
