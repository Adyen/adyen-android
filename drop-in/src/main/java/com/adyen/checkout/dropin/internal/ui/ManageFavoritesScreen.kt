/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageFavoritesScreen(
    navigator: DropInNavigator,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        containerColor = CheckoutThemeProvider.colors.background,
        topBar = {
            DropInTopAppBar(
                // TODO - string resources
                title = "Manage your favorite payment options",
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.back() },
                    ) {
                        // TODO - string resources
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.size(Dimensions.ExtraSmall))

            // TODO - string resources + check copy
            Body(
                text = "Manage payment methods that enhance your speed of checking out",
                color = CheckoutThemeProvider.colors.textSecondary,
                modifier = Modifier.padding(horizontal = Dimensions.Large),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageFavoritesScreenPreview() {
    ManageFavoritesScreen(navigator = DropInNavigator())
}
