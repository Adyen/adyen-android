/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.components.AdyenCheckout

internal class AdyenComponent(
    txVariant: String,
    adyenCheckout: AdyenCheckout,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // TODO - Initialize Payment Flow
    private val paymentFacilitator: PaymentFacilitator =
        PaymentFacilitatorProvider().provide(
            txVariant = txVariant,
            adyenCheckout = adyenCheckout,
            coroutineScope = viewModelScope,
            savedStateHandle = savedStateHandle,
        )

    @Composable
    internal fun ViewFactory(modifier: Modifier = Modifier) {
        paymentFacilitator.ViewFactory(modifier)
    }

    fun observe(lifecycle: Lifecycle) {
        paymentFacilitator.observe(lifecycle)
    }

    fun submit() {
        paymentFacilitator.submit()
    }
}
