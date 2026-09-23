/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/9/2026.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A title and a description to be displayed at the top of a screen.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Title(
            text = title,
            modifier = Modifier.fillMaxWidth(),
        )

        if (subtitle != null) {
            Spacer(Modifier.size(Dimensions.Spacing.ExtraSmall))

            Body(
                text = subtitle,
                color = CheckoutThemeProvider.colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ScreenHeader(
            title = "Cards",
            subtitle = "Enter your card details.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderTitleOnlyPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ScreenHeader(
            title = "Title only",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderLongTextPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        ScreenHeader(
            title = "This is a long title that should wrap",
            subtitle = "This description is also long and therefore it should wrap as well.",
        )
    }
}
