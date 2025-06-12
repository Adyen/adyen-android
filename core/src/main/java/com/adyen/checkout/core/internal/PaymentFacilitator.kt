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
import com.adyen.checkout.core.CheckoutCallback
import com.adyen.checkout.core.PaymentMethodTypes
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.core.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentFacilitator(
    private val coroutineScope: CoroutineScope,
    private val checkoutCallback: CheckoutCallback,
    private val componentEventHandler: ComponentEventHandler<PaymentComponentState<out PaymentMethodDetails>>,
    // TODO - Switch to Component Params
    private val componentParams: ButtonComponentParams,
) {

    // TODO - Make it a val, initialize it based on txVariant?
    private val paymentDelegate: PaymentDelegate<PaymentComponentState<out PaymentMethodDetails>> =
        createPaymentDelegate(PaymentMethodTypes.MB_WAY)

    @Suppress("UNCHECKED_CAST")
    private fun createPaymentDelegate(
        txVariant: String
    ): PaymentDelegate<PaymentComponentState<out PaymentMethodDetails>> {
        return when (txVariant) {
            PaymentMethodTypes.MB_WAY -> MBWayDelegate(coroutineScope, componentParams)
            else -> error("Illegal txVariant")
        } as PaymentDelegate<PaymentComponentState<out PaymentMethodDetails>>
    }

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
                componentEventHandler.onPaymentComponentEvent(event, checkoutCallback)
            }.launchIn(coroutineScope)
    }
}
