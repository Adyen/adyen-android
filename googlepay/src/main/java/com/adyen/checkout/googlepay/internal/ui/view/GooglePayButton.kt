/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.adyen.checkout.googlepay.GooglePayButtonAppearance
import com.adyen.checkout.googlepay.GooglePayButtonTheme
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayButtonViewState
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.helper.isDark
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

@Composable
internal fun GooglePayButton(
    buttonViewState: GooglePayButtonViewState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonAppearance = buttonViewState.buttonAppearance
    val backgroundColor = CheckoutThemeProvider.colors.background
    val defaultButtonTheme = remember(backgroundColor) {
        if (backgroundColor.isDark()) ButtonTheme.Light else ButtonTheme.Dark
    }

    PayButton(
        onClick = onClick,
        allowedPaymentMethods = buttonViewState.allowedPaymentMethods,
        modifier = modifier.fillMaxWidth(),
        theme = buttonAppearance?.theme?.toButtonTheme() ?: defaultButtonTheme,
        type = buttonAppearance?.type?.toButtonType() ?: ButtonType.Buy,
        radius = buttonAppearance?.cornerRadius?.dp ?: DEFAULT_CORNER_RADIUS,
        enabled = !buttonViewState.isLoading,
    )
}

private val DEFAULT_CORNER_RADIUS = 100.dp

@Preview(showBackground = true)
@Composable
private fun GooglePayButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        GooglePayButton(
            buttonViewState = GooglePayButtonViewState(
                allowedPaymentMethods = "[]",
                buttonAppearance = null,
                isLoading = false,
            ),
            onClick = {},
        )
        GooglePayButton(
            buttonViewState = GooglePayButtonViewState(
                allowedPaymentMethods = "[]",
                buttonAppearance = GooglePayButtonAppearance(
                    type = GooglePayButtonType.PAY,
                    cornerRadius = 8,
                ),
                isLoading = false,
            ),
            onClick = {},
        )
        GooglePayButton(
            buttonViewState = GooglePayButtonViewState(
                allowedPaymentMethods = "[]",
                buttonAppearance = GooglePayButtonAppearance(
                    type = GooglePayButtonType.CHECKOUT,
                    cornerRadius = 0,
                ),
                isLoading = true,
            ),
            onClick = {},
        )
        GooglePayButton(
            buttonViewState = GooglePayButtonViewState(
                allowedPaymentMethods = "[]",
                buttonAppearance = GooglePayButtonAppearance(
                    theme = GooglePayButtonTheme.LIGHT,
                ),
                isLoading = false,
            ),
            onClick = {},
        )
        GooglePayButton(
            buttonViewState = GooglePayButtonViewState(
                allowedPaymentMethods = "[]",
                buttonAppearance = GooglePayButtonAppearance(
                    theme = GooglePayButtonTheme.DARK,
                ),
                isLoading = false,
            ),
            onClick = {},
        )
    }
}
