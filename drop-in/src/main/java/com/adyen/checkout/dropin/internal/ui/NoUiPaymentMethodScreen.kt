/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.element.ProgressBar
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * Displays a payment method that takes no input from the shopper. The payments call is already running when this
 * screen appears, so it only reports progress and offers a way out.
 */
@Composable
internal fun NoUiPaymentMethodScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodViewModel,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    NoUiPaymentMethodScreenContent(
        viewState = viewState,
        // The payments call cannot be recalled, so there is nothing to go back to: closing ends Drop-in instead of
        // returning to the payment method list.
        onCloseClick = { navigator.finish() },
    )
}

@Composable
private fun NoUiPaymentMethodScreenContent(
    viewState: PaymentMethodViewState,
    onCloseClick: () -> Unit,
) {
    DropInScaffold(
        navigationIcon = {
            IconButton(
                onClick = onCloseClick,
            ) {
                Icon(Icons.Filled.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
            }
        },
        title = "",
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                // The top inset is deliberately not applied: it reserves the height of a medium top app bar, which
                // here holds only a close icon, and centring below it puts the content under the middle of the screen.
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(Dimensions.Spacing.Large),
        ) {
            CheckoutNetworkLogo(
                txVariant = viewState.logo,
                modifier = Modifier.size(Dimensions.LogoSize.large),
            )

            Spacer(Modifier.size(Dimensions.Spacing.Large))

            Title(
                text = viewState.paymentMethodName,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.size(Dimensions.Spacing.ExtraSmall))

            // TODO - Prototype: hardcoded English, pending a localization key that takes the amount as a format arg.
            Body(
                text = "Starting a transaction for ${viewState.amount}",
                color = CheckoutThemeProvider.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.size(Dimensions.Spacing.ExtraLarge))

            ProgressBar()

            Spacer(Modifier.size(Dimensions.Spacing.Large))

            // TODO - Prototype: hardcoded English, pending a localization key.
            Body(
                text = "Processing..",
                color = CheckoutThemeProvider.colors.textSecondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoUiPaymentMethodScreenContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        NoUiPaymentMethodScreenContent(
            viewState = PaymentMethodViewState(
                paymentMethodName = "iDEAL",
                description = null,
                logo = "ideal",
                amount = "$1,400.32",
            ),
            onCloseClick = {},
        )
    }
}
