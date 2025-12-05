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
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider

internal class AdyenComponent(
    applicationContext: Context,
    savedStateHandle: SavedStateHandle,
    paymentFacilitatorProvider: PaymentFacilitatorProvider,
) : ViewModel() {

    private val paymentFacilitator: PaymentFacilitator =
        paymentFacilitatorProvider.provide(
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

    // TODO - Implement onCleared() function
}
