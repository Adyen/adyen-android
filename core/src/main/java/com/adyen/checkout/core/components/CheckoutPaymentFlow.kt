/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.parcelize.Parcelize

/**
 * A [Composable] that renders the full checkout flow for the given [controller], automatically switching
 * between the payment method input and the action screens as the flow progresses.
 *
 * Secondary screens of the payment method, such as pickers, are displayed on top of the payment method UI.
 *
 * @param controller The [CheckoutController] driving this flow.
 * @param modifier The [Modifier] to be applied to the checkout UI.
 * @param theme An optional [CheckoutTheme] to override the UI styling.
 * @param localizationProvider An optional [CheckoutLocalizationProvider] to override the displayed strings.
 */
@Composable
fun CheckoutPaymentFlow(
    controller: CheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme? = null,
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    var state by rememberSaveable(controller) {
        val initialState = when {
            controller.actionComponent != null -> CheckoutPaymentFlowState.Action
            else -> CheckoutPaymentFlowState.PaymentMethod
        }
        mutableStateOf(initialState)
    }

    LaunchedEffect(controller) {
        controller.navigation.collect { route ->
            state = when (route) {
                is CheckoutRoute.PaymentMethod -> CheckoutPaymentFlowState.PaymentMethod
                is CheckoutRoute.Action -> CheckoutPaymentFlowState.Action
                else -> {
                    adyenLog(AdyenLogLevel.WARN) { "Unknown route: $route" }
                    state
                }
            }
        }
    }

    CheckoutCompositionLocalProvider(
        theme = theme,
        locale = controller.shopperLocale,
        localizationProvider = localizationProvider,
        environment = controller.environment,
    ) {
        CheckoutContent(
            controller = controller,
            modifier = modifier,
            state = state,
        )
    }
}

@Composable
private fun CheckoutContent(
    controller: CheckoutController,
    modifier: Modifier,
    state: CheckoutPaymentFlowState,
) {
    AnimatedContent(state) { localState ->
        when (localState) {
            CheckoutPaymentFlowState.PaymentMethod -> {
                CheckoutPaymentMethodInternal(
                    controller = controller,
                    modifier = modifier,
                )
            }

            CheckoutPaymentFlowState.Action -> {
                CheckoutActionInternal(
                    controller = controller,
                    modifier = modifier,
                )
            }
        }
    }
}

private sealed class CheckoutPaymentFlowState : Parcelable {

    @Parcelize
    data object PaymentMethod : CheckoutPaymentFlowState()

    @Parcelize
    data object Action : CheckoutPaymentFlowState()
}
