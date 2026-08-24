/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job

/**
 * Owns the [CheckoutController] of every payment flow that is currently on the back stack.
 *
 * A controller outlives the screen that created it, because the same payment flow can be displayed by
 * multiple screens. Each flow runs in its own child scope of [parentScope], so it can be cancelled
 * independently when its flow is released.
 */
internal class CheckoutFlowHolder(
    private val parentScope: CoroutineScope,
    private val controllerProvider: CheckoutControllerProvider,
) {

    private val flows = mutableMapOf<DropInPaymentFlowType, CheckoutFlow>()

    fun getController(paymentFlowType: DropInPaymentFlowType): CheckoutController {
        return flows.getOrPut(paymentFlowType) { createFlow(paymentFlowType) }.controller
    }

    /**
     * Releases every flow that is not part of [paymentFlowTypes], cancelling its scope.
     */
    fun retainOnly(paymentFlowTypes: Set<DropInPaymentFlowType>) {
        val iterator = flows.entries.iterator()
        while (iterator.hasNext()) {
            val (paymentFlowType, flow) = iterator.next()
            if (paymentFlowType !in paymentFlowTypes) {
                flow.coroutineScope.cancel()
                iterator.remove()
            }
        }
    }

    private fun createFlow(paymentFlowType: DropInPaymentFlowType): CheckoutFlow {
        val coroutineScope = CoroutineScope(
            parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext.job),
        )
        return CheckoutFlow(
            controller = controllerProvider.provide(paymentFlowType, coroutineScope),
            coroutineScope = coroutineScope,
        )
    }

    private class CheckoutFlow(
        val controller: CheckoutController,
        val coroutineScope: CoroutineScope,
    )
}

internal fun interface CheckoutControllerProvider {

    fun provide(paymentFlowType: DropInPaymentFlowType, coroutineScope: CoroutineScope): CheckoutController
}
