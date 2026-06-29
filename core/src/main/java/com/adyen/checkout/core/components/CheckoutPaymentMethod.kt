/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
fun CheckoutPaymentMethod(
    controller: CheckoutController,
    onNavigate: (CheckoutPaymentMethodRoute) -> Unit,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    val currentOnNavigate by rememberUpdatedState(onNavigate)
    LaunchedEffect(controller) {
        controller.paymentMethodNavigation.collect(currentOnNavigate)
    }

    InternalCheckoutTheme(theme) {
        CheckoutCompositionLocalProvider(
            locale = controller.shopperLocale,
            localizationProvider = localizationProvider,
            environment = controller.environment,
        ) {
            controller.paymentComponent?.Content(modifier)
        }
    }
}
