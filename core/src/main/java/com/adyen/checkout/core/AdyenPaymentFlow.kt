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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
    // TODO - Decide where to create the coroutineScope
    val coroutineScope = rememberCoroutineScope()
    val adyenComponent = viewModel(key = txVariant) {
        AdyenComponent(coroutineScope)
    }
    Column(modifier = modifier) {
        adyenComponent.ViewFactory()
        PayButton(
            onClick = adyenComponent::submit,
        )
    }
}
