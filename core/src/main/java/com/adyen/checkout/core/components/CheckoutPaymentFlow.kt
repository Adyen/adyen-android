/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
fun CheckoutPaymentFlow(
    controller: NewCheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    var state by remember(controller) {
        val initialState = when {
            controller.actionComponent != null -> CheckoutPaymentFlowState.Action
            else -> CheckoutPaymentFlowState.PaymentMethod
        }
        mutableStateOf(initialState)
    }

    when (state) {
        CheckoutPaymentFlowState.PaymentMethod -> {
            CheckoutPaymentMethod(
                controller = controller,
                onNavigate = { route ->
                    state = when (route) {
                        CheckoutRoute.Action -> CheckoutPaymentFlowState.Action
                        is CheckoutRoute.Secondary -> CheckoutPaymentFlowState.Secondary
                    }
                },
                modifier = modifier,
                theme = theme,
                localizationProvider = localizationProvider,
            )
        }

        CheckoutPaymentFlowState.Action -> {
            CheckoutAction(
                controller = controller,
                modifier = modifier,
                theme = theme,
                localizationProvider = localizationProvider,
            )
        }

        CheckoutPaymentFlowState.Secondary -> TODO()
    }
}

private sealed class CheckoutPaymentFlowState {
    data object PaymentMethod : CheckoutPaymentFlowState()
    data object Action : CheckoutPaymentFlowState()
    data object Secondary : CheckoutPaymentFlowState()
}
