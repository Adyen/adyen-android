/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import androidx.annotation.RestrictTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
) {
    val colors = CheckoutThemeProvider.colors

    CircularProgressIndicator(
        modifier = modifier,
        color = colors.primary,
        trackColor = colors.disabled,
    )
}

@Preview(showBackground = true)
@Composable
private fun ProgressBarPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    InternalCheckoutTheme(theme) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
            modifier = Modifier
                .background(CheckoutThemeProvider.colors.background)
                .padding(Dimensions.Spacing.Large),
        ) {
            ProgressBar()
        }
    }
}
