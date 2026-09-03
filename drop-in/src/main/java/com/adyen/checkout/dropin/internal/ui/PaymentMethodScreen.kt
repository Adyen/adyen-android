/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.core.components.CheckoutPaymentMethod
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun PaymentMethodScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodViewModel,
    theme: CheckoutTheme,
) {
    PaymentMethodScreenContent(
        navigator = navigator,
        viewState = viewModel.paymentMethodViewState,
        // TODO - Pass localization provider
        content = { CheckoutPaymentMethod(controller = viewModel.controller, theme = theme) },
    )
}

@Composable
private fun PaymentMethodScreenContent(
    navigator: DropInNavigator,
    viewState: PaymentMethodViewState,
    content: @Composable () -> Unit,
) {
    when (viewState) {
        is PaymentMethodViewState.RegularInput -> RegularPaymentMethodInputScreen(navigator, viewState, content)

        is PaymentMethodViewState.Progress -> PaymentMethodProgressScreen(navigator, viewState)
    }
}

@Composable
internal fun PaymentMethodNavigationIcon(navigator: DropInNavigator) {
    IconButton(
        onClick = { navigator.back() },
    ) {
        if (navigator.isEmptyAfterCurrent()) {
            Icon(Icons.Filled.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
        } else {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, resolveString(CheckoutLocalizationKey.GENERAL_BACK))
        }
    }
}
