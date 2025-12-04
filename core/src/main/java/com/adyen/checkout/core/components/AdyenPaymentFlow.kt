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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.AdyenComponent
import com.adyen.checkout.core.components.internal.DefaultPaymentFacilitatorProvider
import com.adyen.checkout.core.components.internal.StoredPaymentFacilitatorProvider
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
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
    val applicationContext = LocalContext.current.applicationContext
    // TODO - Move Creation Logic to Adyen Checkout
    // TODO - Verify that this does not keep observing the previous values and adds extra observables
    val adyenComponent = viewModel(key = paymentMethod.hashCode().toString()) {
        val paymentFacilitatorProvider = DefaultPaymentFacilitatorProvider(
            paymentMethod = paymentMethod,
            checkoutContext = checkoutContext,
            checkoutCallbacks = checkoutCallbacks,
            checkoutController = checkoutController,
        )

        AdyenComponent(
            applicationContext = applicationContext,
            savedStateHandle = createSavedStateHandle(),
            paymentFacilitatorProvider = paymentFacilitatorProvider,
        )
    }.apply { observe(LocalLifecycleOwner.current.lifecycle) }

    InternalCheckoutTheme(theme) {
        adyenComponent.ViewFactory(modifier, localizationProvider, navigationProvider)
    }
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
    val applicationContext = LocalContext.current.applicationContext
    val storedAdyenComponent = viewModel(key = storedPaymentMethod.hashCode().toString()) {
        val paymentFacilitatorProvider = StoredPaymentFacilitatorProvider(
            storedPaymentMethod = storedPaymentMethod,
            checkoutContext = checkoutContext,
            checkoutCallbacks = checkoutCallbacks,
            checkoutController = checkoutController,
        )

        AdyenComponent(
            applicationContext = applicationContext,
            savedStateHandle = createSavedStateHandle(),
            paymentFacilitatorProvider = paymentFacilitatorProvider,
        )
    }.apply { observe(LocalLifecycleOwner.current.lifecycle) }

    InternalCheckoutTheme(theme) {
        storedAdyenComponent.ViewFactory(modifier, localizationProvider, navigationProvider)
    }
}
