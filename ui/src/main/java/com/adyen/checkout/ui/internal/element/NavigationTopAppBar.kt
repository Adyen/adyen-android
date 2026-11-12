/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/10/2026.
 */

// The navigationIcon slot is named and ordered after its Material 3 counterpart. Renaming it to "content" or moving it
// last would make this component inconsistent with the framework API it wraps.
@file:Suppress("ComposableLambdaParameterNaming", "ComposableLambdaParameterPosition")

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * The app bar of a screen, holding its navigation icon and optionally its title.
 *
 * @param title The title of the screen. When provided, it is shown expanded below the navigation icon and collapses
 * into the bar as the content scrolls. When `null`, only the navigation icon is shown.
 * @param scrollBehavior Connects the app bar to the scrolling content, for example to collapse the title.
 * @param windowInsets The insets the app bar respects. Pass empty insets when a parent already applies them.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavigationTopAppBar(
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    if (title != null) {
        MediumFlexibleTopAppBar(
            title = { Title(title) },
            navigationIcon = navigationIcon,
            windowInsets = windowInsets,
            colors = topAppBarColors(),
            scrollBehavior = scrollBehavior,
            modifier = modifier,
        )
    } else {
        TopAppBar(
            title = {},
            navigationIcon = navigationIcon,
            windowInsets = windowInsets,
            colors = topAppBarColors(),
            scrollBehavior = scrollBehavior,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = CheckoutThemeProvider.colors.background,
    scrolledContainerColor = CheckoutThemeProvider.colors.background,
    navigationIconContentColor = CheckoutThemeProvider.colors.text,
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun NavigationTopAppBarWithTitlePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        NavigationTopAppBar(
            navigationIcon = { PreviewNavigationIcon() },
            title = "Title",
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}

@Preview
@Composable
private fun NavigationTopAppBarWithoutTitlePreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        NavigationTopAppBar(
            navigationIcon = { PreviewNavigationIcon() },
        )
    }
}

@Composable
private fun PreviewNavigationIcon() {
    IconButton(onClick = {}) {
        Icon(
            painter = painterResource(R.drawable.ic_navigation_close),
            contentDescription = null,
        )
    }
}
