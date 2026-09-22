/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/9/2026.
 */

package com.adyen.checkout.authentication.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.authentication.R
import com.adyen.checkout.core.common.internal.ui.IconHeaderContent
import com.adyen.checkout.core.common.internal.ui.PaymentProgressStatus
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.helper.getThemedIcon
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * What the shopper is left looking at while the 3DS2 SDK starts up, and again if the challenge is dismissed: the
 * challenge itself opens on top of this screen rather than replacing it.
 *
 * Unlike the other actions this shows no payment method logo. The screen introduces the security check rather than the
 * card being authenticated, and the icon for it ships with the SDK, so it is picked for the checkout theme rather than
 * loaded for a transaction variant.
 */
@Composable
internal fun AuthenticationContent(
    modifier: Modifier = Modifier,
) {
    val iconResourceId = getThemedIcon(
        backgroundColor = CheckoutThemeProvider.colors.background,
        lightDrawableId = R.drawable.ic_authentication_light,
        darkDrawableId = R.drawable.ic_authentication_dark,
    )

    IconHeaderContent(
        icon = painterResource(iconResourceId),
        title = resolveString(CheckoutLocalizationKey.AUTHENTICATION_TITLE),
        description = resolveString(CheckoutLocalizationKey.AUTHENTICATION_DESCRIPTION),
        modifier = modifier,
    ) {
        PaymentProgressStatus(title = resolveString(CheckoutLocalizationKey.AUTHENTICATION_STATUS))
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthenticationContentPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        AuthenticationContent()
    }
}
