/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import android.os.Parcelable
import androidx.activity.compose.BackHandler
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
import com.adyen.checkout.core.components.internal.CheckoutFullScreenDialog
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme
import kotlinx.parcelize.Parcelize

/**
 * A [Composable] that renders the full checkout flow for the given [controller], automatically switching
 * between the payment method input, action, and secondary screens as the flow progresses.
 *
 * @param controller The [CheckoutController] driving this flow.
 * @param modifier The [Modifier] to be applied to the checkout UI.
 * @param theme The [CheckoutTheme] used to style the UI.
 * @param localizationProvider An optional [CheckoutLocalizationProvider] to override the displayed strings.
 */
@Composable
fun CheckoutPaymentFlow(
    controller: CheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    var state by rememberSaveable(controller) {
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

    InternalCheckoutTheme(theme) {
        CheckoutCompositionLocalProvider(
            locale = controller.shopperLocale,
            localizationProvider = localizationProvider,
            environment = controller.environment,
        ) {
            CheckoutContent(
                controller = controller,
                modifier = modifier,
                state = state,
                onSecondaryDismissed = {
                    state = CheckoutPaymentFlowState.PaymentMethod
                },
            )
        }
    }
}

@Composable
private fun CheckoutContent(
    controller: CheckoutController,
    modifier: Modifier,
    state: CheckoutPaymentFlowState,
    onSecondaryDismissed: () -> Unit,
) {
    // AnimatedContent redraws/animates every time its target state changes
    // when moving from Secondary to PaymentMethod, the state does change but the displayed content is the same
    // this mapping ensures the target state does not change and prevents the AnimatedContent from flickering
    val checkoutContentState = when (state) {
        CheckoutPaymentFlowState.Action -> CheckoutContentState.ACTION
        CheckoutPaymentFlowState.PaymentMethod,
        is CheckoutPaymentFlowState.Secondary -> CheckoutContentState.PAYMENT_METHOD
    }
    AnimatedContent(checkoutContentState) { localState ->
        when (localState) {
            CheckoutContentState.PAYMENT_METHOD -> {
                CheckoutPaymentMethodInternal(
                    controller = controller,
                    modifier = modifier,
                )
            }

            CheckoutContentState.ACTION -> {
                CheckoutActionInternal(
                    controller = controller,
                    modifier = modifier,
                )
            }
        }
    }

    if (state is CheckoutPaymentFlowState.Secondary) {
        CheckoutFullScreenDialog(
            onDismissRequest = onSecondaryDismissed,
        ) {
            CheckoutSecondaryInternal(
                identifier = state.identifier,
                controller = controller,
                modifier = Modifier,
            )
        }
    }
}

private sealed class CheckoutPaymentFlowState : Parcelable {

    @Parcelize
    data object PaymentMethod : CheckoutPaymentFlowState()

    @Parcelize
    data object Action : CheckoutPaymentFlowState()

    @Parcelize
    data class Secondary(val identifier: String) : CheckoutPaymentFlowState()
}

private enum class CheckoutContentState {
    PAYMENT_METHOD,
    ACTION,
}
