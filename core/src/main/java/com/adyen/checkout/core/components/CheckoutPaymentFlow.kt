/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
fun CheckoutPaymentFlow(
    controller: CheckoutController,
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

    BackHandler(state is CheckoutPaymentFlowState.Secondary) {
        state = CheckoutPaymentFlowState.PaymentMethod
    }

    LaunchedEffect(controller) {
        controller.navigation.collect { route ->
            state = when (route) {
                is CheckoutRoute.PaymentMethod -> CheckoutPaymentFlowState.PaymentMethod
                is CheckoutRoute.Action -> CheckoutPaymentFlowState.Action
                is CheckoutRoute.Secondary -> CheckoutPaymentFlowState.Secondary(route.identifier)
                else -> {
                    adyenLog(AdyenLogLevel.WARN) { "Unknown route: $route" }
                    state
                }
            }
        }
    }

    AnimatedContent(state) { localState ->
        when (localState) {
            CheckoutPaymentFlowState.PaymentMethod -> {
                CheckoutPaymentMethod(
                    controller = controller,
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

            is CheckoutPaymentFlowState.Secondary -> {
                CheckoutSecondary(
                    identifier = localState.identifier,
                    controller = controller,
                    modifier = modifier,
                    theme = theme,
                    localizationProvider = localizationProvider,
                )
            }
        }
    }
}

private sealed class CheckoutPaymentFlowState {
    data object PaymentMethod : CheckoutPaymentFlowState()
    data object Action : CheckoutPaymentFlowState()
    data class Secondary(val identifier: String) : CheckoutPaymentFlowState()
}
