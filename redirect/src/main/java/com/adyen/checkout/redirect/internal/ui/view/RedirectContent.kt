/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/9/2026.
 */

package com.adyen.checkout.redirect.internal.ui.view

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
 * What the shopper sees while the redirect is open, and again once they return to the app but before the payment
 * result is known.
 *
 * @param logoTxVariant The transaction variant the payment method logo is loaded for.
 * @param modifier The [Modifier] to be applied to the layout.
 */
@Composable
internal fun RedirectContent(
    logoTxVariant: String,
    modifier: Modifier = Modifier,
) {
    IconHeaderContent(
        logoTxVariant = logoTxVariant,
        title = resolveString(CheckoutLocalizationKey.REDIRECT_TITLE),
        description = resolveString(CheckoutLocalizationKey.REDIRECT_DESCRIPTION),
        modifier = modifier,
    ) {
        PaymentProgressStatus(title = resolveString(CheckoutLocalizationKey.REDIRECT_STATUS))
    }
}

@Preview(showBackground = true)
@Composable
private fun RedirectContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        RedirectContent(logoTxVariant = "ideal")
    }
}
