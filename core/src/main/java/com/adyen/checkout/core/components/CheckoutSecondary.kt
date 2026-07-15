/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/4/2026.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

/**
 * A [Composable] that displays a secondary screen of the payment method handled by the given [controller].
 *
 * A secondary screen is shown when a payment method requires an additional step (identified by [identifier]).
 * The available identifiers are provided through [CheckoutRoute.Secondary].
 *
 * @param identifier The identifier of the secondary screen to display.
 * @param controller The [CheckoutController] driving this flow.
 * @param modifier The [Modifier] to be applied to the secondary UI.
 * @param theme The [CheckoutTheme] used to style the UI.
 * @param localizationProvider An optional [CheckoutLocalizationProvider] to override the displayed strings.
 */
@Composable
fun CheckoutSecondary(
    identifier: String,
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
            (controller.paymentComponent as? SecondaryScreenComponent?)?.SecondaryContent(identifier, modifier)
        }
    }
}
