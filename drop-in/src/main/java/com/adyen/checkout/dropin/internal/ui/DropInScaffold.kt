/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/12/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.PaddingValues
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
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DropInScaffold(
    navigationIcon: @Composable () -> Unit,
    title: String,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        containerColor = CheckoutThemeProvider.colors.background,
        topBar = {
            DropInTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Preview(showBackground = true)
@Composable
private fun DropInScaffoldPreview() {
    InternalCheckoutTheme {
        DropInScaffold(
            navigationIcon = {
                IconButton(
                    onClick = {},
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            },
            title = "Title",
            content = {},
        )
    }
}
