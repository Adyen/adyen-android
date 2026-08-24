/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class TestCheckoutControllerProvider : CheckoutControllerProvider {

    val controllers = mutableMapOf<DropInPaymentFlowType, CheckoutController>()
    val scopes = mutableMapOf<DropInPaymentFlowType, CoroutineScope>()
    val navigationFlows = mutableMapOf<DropInPaymentFlowType, MutableSharedFlow<CheckoutRoute>>()
    var invocations = 0
        private set

    override fun provide(
        paymentFlowType: DropInPaymentFlowType,
        coroutineScope: CoroutineScope,
    ): CheckoutController {
        invocations++
        scopes[paymentFlowType] = coroutineScope
        val navigationFlow = MutableSharedFlow<CheckoutRoute>(extraBufferCapacity = 1)
        navigationFlows[paymentFlowType] = navigationFlow
        val controller = mock<CheckoutController> { on { navigation } doReturn navigationFlow }
        controllers[paymentFlowType] = controller
        return controller
    }
}
