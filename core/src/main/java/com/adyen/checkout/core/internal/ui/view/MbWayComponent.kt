/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/4/2025.
 */

package com.adyen.checkout.core.internal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun MbWayComponent(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // CountryCode
        OutlinedTextField(
            modifier = Modifier
                .onFocusChanged {
                    // TODO uiStateDelegate.onFieldFocusChanged()
                },
            label = {
                Text("Country Code")
            },
            value = "",
            onValueChange = {
                // TODO uiStateDelegate.onFieldValueChanged()
            },
        )

        // PhoneNumber
        OutlinedTextField(
            modifier = Modifier
                .onFocusChanged {
                    // TODO uiStateDelegate.onFieldFocusChanged()
                },
            label = {
                Text("Phone Number")
            },
            value = "",
            onValueChange = {
                // TODO uiStateDelegate.onFieldValueChanged()
            },
        )
    }
}
