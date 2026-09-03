/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.dropin.internal.helper.SavedStateBackStackPersister
import com.adyen.checkout.dropin.internal.ui.element.PaymentMethodHeader
import com.adyen.checkout.dropin.internal.ui.element.PaymentProgressStatus
import com.adyen.checkout.ui.internal.helper.CheckoutThemePreviewWrapper
import com.adyen.checkout.ui.internal.helper.ThemePreviewParameterProvider
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A payment method with nothing to enter and nothing to confirm: it was submitted on arrival, and this only reports
 * that it is under way.
 */
@Composable
internal fun PaymentMethodProgressScreen(
    navigator: DropInNavigator,
    viewState: PaymentMethodViewState.Progress,
) {
    DropInScaffold(
        navigationIcon = { PaymentMethodNavigationIcon(navigator) },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimensions.Spacing.Large),
        ) {
            PaymentMethodHeader(
                logoTxVariant = viewState.logoTxVariant,
                paymentMethodName = viewState.paymentMethodName,
                description = resolveString(CheckoutLocalizationKey.DROP_IN_GENERIC_PAYMENT_METHOD_DESCRIPTION),
            )

            Spacer(Modifier.size(Dimensions.Spacing.QuadrupleExtraLarge))

            PaymentProgressStatus(
                title = resolveString(CheckoutLocalizationKey.DROP_IN_GENERIC_PAYMENT_METHOD_PROGRESS_TITLE),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentMethodProgressScreenPreview(
    @PreviewParameter(ThemePreviewParameterProvider::class) theme: CheckoutTheme,
) {
    CheckoutThemePreviewWrapper(theme) {
        PaymentMethodProgressScreen(
            navigator = DropInNavigator(SavedStateBackStackPersister(SavedStateHandle())),
            viewState = PaymentMethodViewState.Progress(
                logoTxVariant = "ideal",
                paymentMethodName = "iDEAL",
            ),
        )
    }
}
