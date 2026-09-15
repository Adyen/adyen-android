/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.internal.CheckoutFullScreenDialog
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.SecondaryNavigationEvent
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A [Composable] that displays the payment method input UI for the given [controller].
 *
 * Use this when you want to render the payment method screen yourself. To render the whole flow
 * (payment method, action and secondary screens) automatically, use [CheckoutPaymentFlow] instead.
 *
 * @param controller The [CheckoutController] driving this flow.
 * @param modifier The [Modifier] to be applied to the payment method UI.
 * @param theme The [CheckoutTheme] used to style the UI.
 * @param localizationProvider An optional [CheckoutLocalizationProvider] to override the displayed strings.
 */
@Composable
fun CheckoutPaymentMethod(
    controller: CheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    CheckoutCompositionLocalProvider(
        theme = theme,
        locale = controller.shopperLocale,
        localizationProvider = localizationProvider,
        environment = controller.environment,
    ) {
        CheckoutPaymentMethodInternal(controller, modifier)
    }
}

@Composable
internal fun CheckoutPaymentMethodInternal(controller: CheckoutController, modifier: Modifier) {
    val paymentComponent = controller.paymentComponent ?: return
    if (paymentComponent !is SecondaryScreenComponent) {
        paymentComponent.Content(modifier)
        return
    }
    SecondaryScreenHost(paymentComponent, modifier)
}

@Composable
private fun <T> SecondaryScreenHost(
    component: T,
    modifier: Modifier,
) where T : PaymentComponent, T : SecondaryScreenComponent {
    val backStack = rememberSaveable(component) { mutableStateListOf<String>() }

    component.Content(modifier)

    backStack.lastOrNull()?.let { key ->
        key(key) {
            val navigationIcon = if (backStack.size > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close
            CheckoutFullScreenDialog(
                onDismissRequest = { backStack.removeLastOrNull() },
                navigationIcon = navigationIcon,
            ) {
                component.SecondaryContent(key, Modifier)
            }
        }
    }

    LaunchedEffect(component) {
        component.navigation.collect { event ->
            when (event) {
                is SecondaryNavigationEvent.Open -> {
                    backStack.add(event.key)
                }

                SecondaryNavigationEvent.Close -> {
                    backStack.removeLastOrNull()
                }
            }
        }
    }
}
