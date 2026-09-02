/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/8/2026.
 */

// The footer slot is not the content of this component, which builds its own. Renaming it to "content" would describe
// it wrongly at every call site.
@file:Suppress("ComposableLambdaParameterNaming")

package com.adyen.checkout.dropin.internal.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.ui.internal.element.ProgressBar
import com.adyen.checkout.ui.internal.element.button.SecondaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * Reports that a payment is under way and that the shopper has nothing left to do.
 *
 * Shown whenever a payment flow occupies the screen without taking any input: while an action that needs no
 * interaction is being handled, and while a payment method that has no UI is being submitted.
 *
 * @param logoTxVariant The transaction variant the payment method logo is loaded for.
 * @param paymentMethodName The name of the payment method, shown as the title.
 * @param description The line explaining what is happening. Supplied by the caller because it is the one part that
 * differs per case — a redirect explains where the shopper is being sent, a payment method without UI names the
 * amount being charged.
 * @param progressTitle The line shown with the progress indicator, naming what is being waited on.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param footer The optional bottom section for whatever way out the case offers — a way to abandon a redirect, or a
 * way to leave once the payment is done. It is pinned to the bottom and the progress block centres in what is left,
 * so the content above reads the same with and without it.
 */
@Composable
internal fun PaymentProgressContent(
    logoTxVariant: String,
    paymentMethodName: String,
    description: String,
    progressTitle: String,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        PaymentProgressBody(
            logoTxVariant = logoTxVariant,
            paymentMethodName = paymentMethodName,
            description = description,
            progressTitle = progressTitle,
            modifier = Modifier.weight(1f),
        )

        footer?.invoke()
    }
}

@Composable
private fun PaymentProgressBody(
    logoTxVariant: String,
    paymentMethodName: String,
    description: String,
    progressTitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth(),
    ) {
        CheckoutNetworkLogo(
            txVariant = logoTxVariant,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.LogoSize.large),
        )

        Spacer(Modifier.size(Dimensions.Spacing.DoubleExtraLarge))

        Title(
            text = paymentMethodName,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.size(Dimensions.Spacing.Small))

        Body(
            text = description,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.size(Dimensions.Spacing.QuadrupleExtraLarge))

        PaymentProgressIndicator(progressTitle)
    }
}

@Composable
private fun PaymentProgressIndicator(
    progressTitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.Large),
        modifier = modifier.fillMaxWidth(),
    ) {
        ProgressBar()

        Body(
            text = progressTitle,
            color = CheckoutThemeProvider.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentProgressContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PaymentProgressContent(
            logoTxVariant = "ideal",
            paymentMethodName = "iDEAL",
            description = "You are being redirected to iDEAL where you can complete the transaction.",
            progressTitle = "Processing payment",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentProgressContentWithFooterPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PaymentProgressContent(
            logoTxVariant = "ideal",
            paymentMethodName = "iDEAL",
            description = "You are being redirected to iDEAL where you can complete the transaction.",
            progressTitle = "Processing payment",
            footer = {
                SecondaryButton(
                    onClick = {},
                    text = "Cancel",
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
}
