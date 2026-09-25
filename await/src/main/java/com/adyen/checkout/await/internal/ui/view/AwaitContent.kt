/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/7/2025.
 */

package com.adyen.checkout.await.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.internal.ui.IconHeaderContent
import com.adyen.checkout.core.common.internal.ui.PaymentProgressStatus
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * What the shopper sees while the payment is completed elsewhere — in the payment method's own app, or on its website —
 * and the status is polled from here until it resolves.
 *
 * @param logoTxVariant The transaction variant the payment method logo is loaded for.
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
internal fun AwaitContent(
    logoTxVariant: String,
    modifier: Modifier = Modifier,
) {
    IconHeaderContent(
        logoTxVariant = logoTxVariant,
        title = resolveString(CheckoutLocalizationKey.AWAIT_TITLE),
        description = resolveString(CheckoutLocalizationKey.AWAIT_DESCRIPTION),
        modifier = modifier,
    ) {
        PaymentProgressStatus(title = resolveString(CheckoutLocalizationKey.AWAIT_STATUS))
    }
}

@Preview(showBackground = true)
@Composable
private fun AwaitContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AwaitContent(logoTxVariant = "blik")
    }
}
