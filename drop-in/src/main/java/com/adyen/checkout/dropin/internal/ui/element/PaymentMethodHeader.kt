/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui.element

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.internal.ui.CheckoutNetworkLogo
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.text.Title
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * Used by every screen that does not introduce itself through a collapsing app bar title — anything stored, and any
 * state the shopper cannot type into. Screens arrange it themselves, because where it sits differs: centred in the
 * empty space when there is nothing to do, at the top when a form follows it.
 *
 * @param logoTxVariant The transaction variant the payment method logo is loaded for.
 * @param paymentMethodName What to call the payment method, such as its brand or its last four digits.
 * @param description The line about what is being paid or what is happening, if there is one to show.
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
internal fun PaymentMethodHeader(
    logoTxVariant: String,
    paymentMethodName: String,
    description: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
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

        if (!description.isNullOrBlank()) {
            Spacer(Modifier.size(Dimensions.Spacing.Small))

            Body(
                text = description,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodHeaderPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PaymentMethodHeader(
            logoTxVariant = "visa",
            paymentMethodName = "•••• 4556",
            description = "Use Visa to pay $140.98.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodHeaderWithoutDescriptionPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PaymentMethodHeader(
            logoTxVariant = "ideal",
            paymentMethodName = "iDEAL",
            description = null,
        )
    }
}
