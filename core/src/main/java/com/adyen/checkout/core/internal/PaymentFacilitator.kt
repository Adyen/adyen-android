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
import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.internal.ui.model.SessionParamsFactory
import com.adyen.checkout.core.mbway.internal.ui.MBWayComponentState
import com.adyen.checkout.core.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.core.mbway.internal.ui.getMBWayConfiguration
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

// TODO - Have separate facilitators for sessions and advanced?
internal class PaymentFacilitator(
    private val coroutineScope: CoroutineScope,
    private val checkoutSession: CheckoutSession?,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val checkoutCallback: CheckoutCallback,
    private val sessionInteractor: SessionInteractor?
) {

    private val componentParams = ButtonComponentParamsMapper(CommonComponentParamsMapper()).mapToParams(
        checkoutConfiguration = checkoutConfiguration,

        // TODO - Add locale support, For now it's hardcoded to US
//        deviceLocale = localeProvider.getLocale(application)
        deviceLocale = Locale.US,
        dropInOverrideParams = null,
        componentSessionParams = checkoutSession?.let {
            SessionParamsFactory.create(checkoutSession)
        },
        componentConfiguration = checkoutConfiguration.getMBWayConfiguration(),
    )

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
