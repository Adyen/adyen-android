/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adyen.checkout.core.internal.AdyenComponent
import com.adyen.checkout.core.internal.ui.view.PayButton

// TODO - Change Name?
@Suppress("UnusedParameter")
@Composable
fun AdyenPaymentFlow(
    txVariant: String,
    adyenCheckout: AdyenCheckout,
    modifier: Modifier = Modifier,
) {
    // TODO - Move Creation Logic to Adyen Checkout
    val adyenComponent = viewModel(key = txVariant) {
        AdyenComponent(adyenCheckout)
    }.apply { observe(LocalLifecycleOwner.current.lifecycle) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        adyenComponent.ViewFactory()
        PayButton(
            onClick = adyenComponent::submit,
        )
    }
}
