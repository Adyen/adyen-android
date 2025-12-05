/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.DefaultPaymentFacilitatorProvider
import com.adyen.checkout.core.components.internal.StoredPaymentFacilitatorProvider
import com.adyen.checkout.core.components.internal.ui.view.InternalAdyenPaymentFlow
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider
import com.adyen.checkout.ui.theme.CheckoutTheme

// TODO - Change Name?
@Composable
fun AdyenPaymentFlow(
    paymentMethod: PaymentMethod,
    checkoutContext: CheckoutContext,
    checkoutCallbacks: CheckoutCallbacks,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    checkoutController: CheckoutController = rememberCheckoutController(),
    localizationProvider: CheckoutLocalizationProvider? = null,
    navigationProvider: CheckoutNavigationProvider? = null,
) {
    val paymentFacilitatorProvider = DefaultPaymentFacilitatorProvider(
        paymentMethod = paymentMethod,
        checkoutContext = checkoutContext,
        checkoutCallbacks = checkoutCallbacks,
        checkoutController = checkoutController,
    )

    InternalAdyenPaymentFlow(
        key = paymentMethod.hashCode().toString(),
        paymentFacilitatorProvider = paymentFacilitatorProvider,
        modifier = modifier,
        theme = theme,
        localizationProvider = localizationProvider,
        navigationProvider = navigationProvider,
    )
}

@Composable
fun AdyenPaymentFlow(
    storedPaymentMethod: StoredPaymentMethod,
    checkoutContext: CheckoutContext,
    checkoutCallbacks: CheckoutCallbacks,
    modifier: Modifier = Modifier,
    theme: CheckoutTheme = CheckoutTheme(),
    checkoutController: CheckoutController = rememberCheckoutController(),
    localizationProvider: CheckoutLocalizationProvider? = null,
    navigationProvider: CheckoutNavigationProvider? = null,
) {
    val paymentFacilitatorProvider = StoredPaymentFacilitatorProvider(
        storedPaymentMethod = storedPaymentMethod,
        checkoutContext = checkoutContext,
        checkoutCallbacks = checkoutCallbacks,
        checkoutController = checkoutController,
    )

    InternalAdyenPaymentFlow(
        key = storedPaymentMethod.hashCode().toString(),
        paymentFacilitatorProvider = paymentFacilitatorProvider,
        modifier = modifier,
        theme = theme,
        localizationProvider = localizationProvider,
        navigationProvider = navigationProvider,
    )
}
