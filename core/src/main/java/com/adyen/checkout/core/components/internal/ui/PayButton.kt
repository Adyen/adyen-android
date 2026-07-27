/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/7/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.adyen.checkout.core.common.internal.helper.LocalLocale
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.format
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.ui.internal.element.button.PrimaryButton
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun PayButton(
    payButtonViewState: PayButtonViewState,
    onClick: () -> Unit,
) {
    val amount = payButtonViewState.amount
    val text = when {
        amount == null -> resolveString(CheckoutLocalizationKey.PAY_BUTTON_NO_AMOUNT)
        amount.value == 0L -> resolveString(CheckoutLocalizationKey.PAY_BUTTON_ZERO_AMOUNT)
        else -> resolveString(CheckoutLocalizationKey.PAY_BUTTON_WITH_AMOUNT, amount.format(LocalLocale.current))
    }

    PrimaryButton(
        onClick = onClick,
        text = text,
        isLoading = payButtonViewState.isLoading,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
private fun PayButtonPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PayButton(
            payButtonViewState = PayButtonViewState(
                amount = Amount(currency = "EUR", value = 1337),
                isLoading = false,
            ),
            onClick = {},
        )
        PayButton(
            payButtonViewState = PayButtonViewState(
                amount = Amount(currency = "EUR", value = 0),
                isLoading = false,
            ),
            onClick = {},
        )
        PayButton(
            payButtonViewState = PayButtonViewState(
                amount = null,
                isLoading = false,
            ),
            onClick = {},
        )
        PayButton(
            payButtonViewState = PayButtonViewState(
                amount = Amount(currency = "EUR", value = 1337),
                isLoading = true,
            ),
            onClick = {},
        )
    }
}
