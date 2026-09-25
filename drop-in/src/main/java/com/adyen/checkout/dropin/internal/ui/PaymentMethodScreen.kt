/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/1/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.internal.ui.NavigationBackButton
import com.adyen.checkout.core.common.internal.ui.NavigationCloseButton
import com.adyen.checkout.core.components.CheckoutPaymentMethod

@Composable
internal fun PaymentMethodScreen(
    navigator: DropInNavigator,
    viewModel: PaymentMethodViewModel,
) {
    when (val viewState = viewModel.paymentMethodViewState) {
        is PaymentMethodViewState.Regular -> RegularPaymentMethodContent(navigator, viewState) {
            CheckoutPaymentMethod(controller = viewModel.controller)
        }

        is PaymentMethodViewState.Stored -> StoredPaymentMethodContent(navigator, viewState) {
            CheckoutPaymentMethod(controller = viewModel.controller)
        }

        is PaymentMethodViewState.Progress -> PaymentMethodProgressContent(navigator, viewState)
    }
}

@Composable
internal fun PaymentMethodNavigationIcon(navigator: DropInNavigator) {
    if (navigator.isEmptyAfterCurrent()) {
        NavigationCloseButton(onClick = { navigator.back() })
    } else {
        NavigationBackButton(onClick = { navigator.back() })
    }
}
