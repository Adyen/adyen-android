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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentFacilitator(
    private val paymentDelegate: PaymentDelegate<BaseComponentState>,
    private val coroutineScope: CoroutineScope,
    private val componentEventHandler: ComponentEventHandler<BaseComponentState>,
) {

    @Composable
    fun ViewFactory(modifier: Modifier = Modifier) {
        paymentDelegate.ViewFactory(modifier)
    }

    fun submit() {
        paymentDelegate.submit()
    }

    fun observe(lifecycle: Lifecycle) {
        paymentDelegate.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                componentEventHandler.onPaymentComponentEvent(event)
            }.launchIn(coroutineScope)
    }
}
