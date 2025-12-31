/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.AdyenPaymentFlowKey
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider

internal class AdyenComponent(
    key: AdyenPaymentFlowKey,
    checkoutContext: CheckoutContext,
    checkoutCallbacks: CheckoutCallbacks,
    checkoutController: CheckoutController,
    applicationContext: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val paymentFacilitator: PaymentFacilitator =
        PaymentFacilitatorProvider().provide(
            key = key,
            checkoutContext = checkoutContext,
            checkoutCallbacks = checkoutCallbacks,
            checkoutController = checkoutController,
            applicationContext = applicationContext,
            coroutineScope = viewModelScope,
            savedStateHandle = savedStateHandle,
        )

    @Composable
    internal fun ViewFactory(
        modifier: Modifier,
        localizationProvider: CheckoutLocalizationProvider?,
        navigationProvider: CheckoutNavigationProvider?,
    ) {
        paymentFacilitator.ViewFactory(modifier, localizationProvider, navigationProvider)
    }

    fun observe(lifecycle: Lifecycle) {
        paymentFacilitator.observe(lifecycle)
    }

    override fun onCleared() {
        super.onCleared()
        paymentFacilitator.onCleared()
    }
}
