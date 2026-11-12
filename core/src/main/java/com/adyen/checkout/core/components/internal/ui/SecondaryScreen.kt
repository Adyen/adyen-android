/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.internal.ui.NavigationBackButton
import com.adyen.checkout.core.common.internal.ui.NavigationCloseButton
import com.adyen.checkout.core.common.internal.ui.ScreenHeader
import com.adyen.checkout.ui.internal.element.NavigationTopAppBar
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun SecondaryScreen(
    isNested: Boolean,
    onNavigationClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column {
        NavigationTopAppBar(
            navigationIcon = {
                if (isNested) {
                    NavigationBackButton(onClick = onNavigationClick)
                } else {
                    NavigationCloseButton(onClick = onNavigationClick)
                }
            },
            windowInsets = WindowInsets(0),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimensions.Spacing.Large)
                .verticalScroll(rememberScrollState()),
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryScreenPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        SecondaryScreen(
            isNested = false,
            onNavigationClick = {},
        ) {
            ScreenHeader(
                title = "Title",
                subtitle = "Subtitle",
            )
        }
    }
}
