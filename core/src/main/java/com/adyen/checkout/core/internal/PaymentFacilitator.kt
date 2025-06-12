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
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.core.mbway.internal.ui.MBWayComponentState
import com.adyen.checkout.core.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.core.sessions.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentFacilitator(
    private val coroutineScope: CoroutineScope,
    private val checkoutCallback: CheckoutCallback,
    private val sessionInteractor: SessionInteractor?,

    // TODO - Switch to Component Params
    private val componentParams: ButtonComponentParams,
) {

    // TODO - Make it a val, initialize it based on txVariant?
    private val paymentDelegate: PaymentDelegate<MBWayComponentState> = MBWayDelegate(coroutineScope, componentParams)

    // TODO - Refactor PaymentFacilitator to take ComponentEventHandler as a parameter (either an advanced or
    //  a sessions one). This will make PaymentFacilitator agnostic to flow type.
    private val componentEventHandler = DefaultComponentEventHandler<MBWayComponentState>(sessionInteractor)

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
