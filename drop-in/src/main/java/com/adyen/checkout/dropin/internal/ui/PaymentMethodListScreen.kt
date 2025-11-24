/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PaymentMethodListScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodListViewModel,
) {
    Scaffold(
        containerColor = CheckoutThemeProvider.colors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CheckoutThemeProvider.colors.background,
                    navigationIconContentColor = CheckoutThemeProvider.colors.text,
                ),
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.back() },
                    ) {
                        // TODO - String resources
                        Icon(Icons.Default.Close, "Close")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = Dimensions.Large),
        ) {
            Title("Payment method list")
        }
    }
}
