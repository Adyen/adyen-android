/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/12/2025.
 */

package com.adyen.checkout.core.components.internal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.internal.AdyenComponent
import com.adyen.checkout.core.components.internal.PaymentFacilitatorProvider
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider
import com.adyen.checkout.ui.internal.theme.InternalCheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun InternalAdyenPaymentFlow(
    key: String,
    paymentFacilitatorProvider: PaymentFacilitatorProvider,
    modifier: Modifier,
    theme: CheckoutTheme,
    localizationProvider: CheckoutLocalizationProvider?,
    navigationProvider: CheckoutNavigationProvider?,
) {
    val applicationContext = LocalContext.current.applicationContext
    // TODO - Move Creation Logic to Adyen Checkout
    // TODO - Verify that this does not keep observing the previous values and adds extra observables
    val adyenComponent = viewModel(key = key) {
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
