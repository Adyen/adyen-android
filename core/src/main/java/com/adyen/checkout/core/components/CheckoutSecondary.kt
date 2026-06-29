/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
fun CheckoutSecondary(
    identifier: String,
    controller: CheckoutController,
    onNavigate: (CheckoutSecondaryRoute) -> Unit,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    val currentOnNavigate by rememberUpdatedState(onNavigate)
    LaunchedEffect(controller) {
        controller.secondaryNavigation.collect(currentOnNavigate)
    }

    InternalCheckoutTheme(theme) {
        CheckoutCompositionLocalProvider(
            locale = controller.shopperLocale,
            localizationProvider = localizationProvider,
            environment = controller.environment,
        ) {
            (controller.paymentComponent as? SecondaryScreenComponent?)?.SecondaryContent(identifier, modifier)
        }
    }
}
