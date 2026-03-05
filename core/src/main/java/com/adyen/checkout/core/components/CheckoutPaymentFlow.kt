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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.internal.CheckoutControllerState
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme
import java.util.Locale

@Composable
fun CheckoutPaymentFlow(
    controller: NewCheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    val state by controller.state.collectAsStateWithLifecycle()

    InternalCheckoutTheme(theme) {
        // TODO - get params from controller
        CheckoutCompositionLocalProvider(
            locale = Locale.getDefault(),
            localizationProvider = localizationProvider,
            environment = Environment.TEST,
        ) {
            when (val localState = state) {
                is CheckoutControllerState.PaymentMethod -> {
                    val provider = remember { PaymentMethodProvider.get(localState.paymentMethod) }
                    provider?.PaymentComponent(modifier)
                }

                is CheckoutControllerState.Action -> TODO()
            }
        }
    }
}
