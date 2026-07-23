/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A [Composable] that displays the UI of an action being handled by the given [controller].
 *
 * Use this when you want to render the action screen yourself. To render the whole flow
 * (payment method, action and secondary screens) automatically, use [CheckoutPaymentFlow] instead.
 *
 * @param controller The [CheckoutController] driving this flow.
 * @param modifier The [Modifier] to be applied to the action UI.
 * @param theme The [CheckoutTheme] used to style the UI.
 * @param localizationProvider An optional [CheckoutLocalizationProvider] to override the displayed strings.
 */
@Composable
fun CheckoutAction(
    controller: CheckoutController,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    localizationProvider: CheckoutLocalizationProvider? = null,
) {
    InternalCheckoutTheme(theme) {
        CheckoutCompositionLocalProvider(
            locale = controller.shopperLocale,
            localizationProvider = localizationProvider,
            environment = controller.environment,
        ) {
            CheckoutActionInternal(controller, modifier)
        }
    }
}

@Composable
internal fun CheckoutActionInternal(
    controller: CheckoutController,
    modifier: Modifier,
) {
    controller.actionComponent?.Content(modifier)
}
