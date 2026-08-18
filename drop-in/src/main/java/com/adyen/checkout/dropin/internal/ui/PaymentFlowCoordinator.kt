/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 13/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Owns the lifecycle of the [CheckoutController] that drives the payment flow of a single payment method.
 *
 * Only one flow can be active at a time. Starting a new flow tears the previous one down. A flow stays alive as long
 * as at least one [PaymentFlowNavKey] is on the back stack, and is torn down as soon as none is left.
 */
internal class PaymentFlowCoordinator(
    private val navigator: DropInNavigator,
    private val controllerProvider: DropInControllerProvider,
    private val coroutineScope: CoroutineScope,
) {

    private var activeFlow: ActiveFlow? = null

    /**
     * The controller of the active flow, or `null` if no flow is active.
     */
    val activeController: CheckoutController? get() = activeFlow?.controller

    init {
        observeBackStack()
    }

    /**
     * Starts a new flow for [paymentFlowType] and navigates to its payment method screen.
     *
     * @param replaceBackStack Whether the current back stack should be replaced instead of being added to.
     */
    fun startFlow(paymentFlowType: DropInPaymentFlowType, replaceBackStack: Boolean = false) {
        createFlow(paymentFlowType)

        val key = PaymentMethodNavKey(paymentFlowType)
        if (replaceBackStack) {
            navigator.clearAndNavigateTo(key)
        } else {
            navigator.navigateTo(key)
        }
    }

    /**
     * Starts a new flow for a [paymentFlowType] that is already on the back stack, without navigating.
     *
     * A controller cannot be restored, therefore the flow is started from scratch.
     */
    // TODO - Revisit when action and result screens are added: when they are pushed on top of the payment method
    //  screen, its key is no longer last and the flow is not restored.
    // TODO - Only the navigation is restored. Restoring the contents of the flow (entered input, an in-flight submit or
    //  an in-progress redirect) requires component state saving in the core module.
    fun restoreFlow() {
        when (val key = navigator.backStack.lastOrNull()) {
            is PaymentMethodNavKey -> createFlow(key.paymentFlowType)
            else -> Unit
        }
    }

    private fun createFlow(paymentFlowType: DropInPaymentFlowType) {
        cancelActiveFlow()

        val flowScope = createFlowScope()
        activeFlow = ActiveFlow(
            controller = controllerProvider.provide(paymentFlowType, flowScope),
            coroutineScope = flowScope,
        )
    }

    private fun observeBackStack() {
        coroutineScope.launch {
            navigator.backStackFlow.collect { backStack ->
                if (backStack.none { it is PaymentFlowNavKey }) {
                    cancelActiveFlow()
                }
            }
        }
    }

    private fun cancelActiveFlow() {
        activeFlow?.coroutineScope?.cancel()
        activeFlow = null
    }

    /**
     * Creates a child scope of [coroutineScope]: cancelling the parent cancels the flow, but cancelling the flow
     * leaves the parent untouched. [SupervisorJob] makes sure a failure inside the flow does not cancel Drop-in.
     */
    private fun createFlowScope(): CoroutineScope {
        val parentContext = coroutineScope.coroutineContext
        return CoroutineScope(parentContext + SupervisorJob(parentContext[Job]))
    }

    private class ActiveFlow(
        val controller: CheckoutController,
        val coroutineScope: CoroutineScope,
    )
}
