/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import com.adyen.checkout.core.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId

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
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.COUNTRY_CODE, focusState.hasFocus)
                },
            label = {
                Text("Country Code")
            },
            value = viewState.countryCodeFieldState.value,
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.COUNTRY_CODE, value)
            },
        )

        // PhoneNumber
        OutlinedTextField(
            modifier = Modifier
                .onFocusChanged { focusState ->
                    fieldChangeListener.onFieldFocusChanged(MBWayFieldId.PHONE_NUMBER, focusState.hasFocus)
                },
            label = {
                Text("Phone Number")
            },
            value = viewState.phoneNumberFieldState.value,
            onValueChange = { value ->
                fieldChangeListener.onFieldValueChanged(MBWayFieldId.PHONE_NUMBER, value)
            },
        )
    }
}
