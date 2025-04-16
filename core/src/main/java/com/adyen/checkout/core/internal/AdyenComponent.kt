/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel

internal class AdyenComponent : ViewModel() {

    private val paymentFacilitator: PaymentFacilitator

    init {
        // TODO - Initialize Payment Flow
        paymentFacilitator = PaymentFacilitator()
    }

    @Composable
    internal fun ViewFactory(modifier: Modifier = Modifier) {
        paymentFacilitator.ViewFactory(modifier)
    }

    fun submit() {
        paymentFacilitator.submit()
    }
}
