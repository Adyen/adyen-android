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
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A screen that introduces itself with a title, collapsing it as the content scrolls.
 *
 * @param modifier The [Modifier] to be applied to the layout. The nested scroll connection the collapsing title
 * needs is applied on top of it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DropInScaffold(
    navigationIcon: @Composable () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
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
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        content(innerPadding)
    }
}

/**
 * A screen with no title of its own.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
internal fun DropInScaffold(
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = CheckoutThemeProvider.colors.background,
        topBar = {
            DropInTopAppBar(navigationIcon = navigationIcon)
        },
        modifier = modifier,
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Preview(showBackground = true)
@Composable
private fun DropInScaffoldPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
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

@Preview(showBackground = true)
@Composable
private fun DropInScaffoldWithoutTitlePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        DropInScaffold(
            navigationIcon = {
                IconButton(
                    onClick = {},
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            },
            content = {},
        )
    }
}
