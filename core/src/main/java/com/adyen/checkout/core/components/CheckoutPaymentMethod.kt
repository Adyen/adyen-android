/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
fun CheckoutPaymentMethod(
    controller: CheckoutController,
    onNavigate: (CheckoutRoute) -> Unit,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    SideEffect {
        controller.onNavigate = onNavigate
    }

    InternalCheckoutTheme(theme) {
        // TODO - get params from controller
        CheckoutCompositionLocalProvider(
            locale = LocalLocale.current.platformLocale,
            localizationProvider = localizationProvider,
            environment = Environment.TEST,
        ) {
            controller.paymentComponent?.Content(modifier)
        }
    }
}
