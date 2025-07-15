/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.core.components.internal.AdyenComponent
import com.adyen.checkout.ui.internal.PrimaryButton

// TODO - Change Name?
@Composable
fun AdyenPaymentFlow(
    txVariant: String,
    adyenCheckout: AdyenCheckout,
    modifier: Modifier = Modifier,
    checkoutController: CheckoutController = rememberCheckoutController(),
) {
    // TODO - Move Creation Logic to Adyen Checkout
    val adyenComponent = viewModel(key = txVariant) {
        AdyenComponent(
            txVariant = txVariant,
            adyenCheckout = adyenCheckout,
            savedStateHandle = createSavedStateHandle(),
            checkoutController = checkoutController,
        )
    }.apply { observe(LocalLifecycleOwner.current.lifecycle) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        adyenComponent.ViewFactory()

        Spacer(Modifier.size(16.dp))

        // TODO - Properly implement the pay button
        PrimaryButton(
            onClick = adyenComponent::submit,
            text = "Pay $13.37",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
