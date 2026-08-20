/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/8/2026.
 */

package com.adyen.checkout.authentication.internal.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.ui.internal.element.ProgressBar
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.text.Body
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * Shown while 3DS2 runs: first during the invisible fingerprint, then behind the challenge screen the 3DS2 SDK opens
 * on top of it.
 */
@Composable
internal fun AuthenticationContent(
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        ProgressBar()
        Spacer(Modifier.size(Dimensions.Spacing.Large))
        // TODO - Prototype: this needs its own CheckoutLocalizationKey, which is a public API change.
        Body("Authenticating the payment…")
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
