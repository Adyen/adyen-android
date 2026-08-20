/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 13/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Owns the lifecycle of the [CheckoutController] that drives the payment flow of a single payment method.
 *
 * Only one flow can be active at a time. Starting a new flow tears the previous one down. A flow stays alive as long
 * as at least one [PaymentFlowNavKey] is on the back stack, and is torn down as soon as none is left.
 *
 * Google Pay is the exception: its component is rendered on the payment method list and has to be alive before the
 * shopper picks anything, so it gets a standby flow of its own that outlives the active one. It is promoted to the
 * active flow once it returns an action.
 */
internal class PaymentFlowCoordinator(
    private val navigator: DropInNavigator,
    private val controllerProvider: DropInControllerProvider,
    private val coroutineScope: CoroutineScope,
) {

    private var activeFlow: ActiveFlow? = null

    private var googlePayFlow: ActiveFlow? = null

    /**
     * The controller of the active flow, or `null` if no flow is active.
     */
    val activeController: CheckoutController? get() = activeFlow?.controller

    /**
     * The controller that renders the Google Pay button on the payment method list, or `null` if Google Pay is not
     * offered.
     */
    val googlePayController: CheckoutController? get() = googlePayFlow?.controller

    init {
        observeBackStack()
    }

    /**
     * Starts a new flow for [paymentFlowType] and navigates to its screen.
     *
     * A payment method that takes no input from the shopper gets a screen of its own and is submitted right away, so
     * the payments call runs while that screen reports progress. The flow then continues on the action screen, or
     * finishes Drop-in if no action is required.
     *
     * @param replaceBackStack Whether the current back stack should be replaced instead of being added to.
     */
    fun startFlow(paymentFlowType: DropInPaymentFlowType, replaceBackStack: Boolean = false) {
        val controller = startActiveFlow(paymentFlowType).controller
        val requiresUserInteraction = controller.requiresUserInteraction()

        val key = if (requiresUserInteraction) {
            PaymentMethodNavKey(paymentFlowType)
        } else {
            NoUiPaymentMethodNavKey(paymentFlowType)
        }

        if (replaceBackStack) {
            navigator.clearAndNavigateTo(key)
        } else {
            navigator.navigateTo(key)
        }

        // Submitted only after navigating, so the screen reporting progress is already on the back stack by the time
        // the payments call returns an action and replaces it.
        if (!requiresUserInteraction) {
            controller.submit()
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
    //  an in-progress redirect) requires component state saving in the core module. For a payment method without UI
    //  this means the payments call is not resumed, leaving that screen reporting progress that never completes.
    fun restoreFlow() {
        when (val key = navigator.backStack.lastOrNull()) {
            is PaymentMethodNavKey -> startActiveFlow(key.paymentFlowType)
            is NoUiPaymentMethodNavKey -> startActiveFlow(key.paymentFlowType)
            else -> Unit
        }
    }

    /**
     * Creates the standby flow that renders the Google Pay button on the payment method list. It is deliberately not
     * the active flow: tapping another payment method must not tear it down.
     */
    fun prepareGooglePayFlow(paymentFlowType: DropInPaymentFlowType) {
        if (googlePayFlow != null) return

        val flow = createFlow(paymentFlowType)
        googlePayFlow = flow
        observeNavigation(flow)
    }

    private fun startActiveFlow(paymentFlowType: DropInPaymentFlowType): ActiveFlow {
        cancelActiveFlow()

        val flow = createFlow(paymentFlowType)
        activeFlow = flow
        observeNavigation(flow)
        return flow
    }

    private fun createFlow(paymentFlowType: DropInPaymentFlowType): ActiveFlow {
        val flowScope = createFlowScope()
        return ActiveFlow(
            controller = controllerProvider.provide(paymentFlowType, flowScope),
            coroutineScope = flowScope,
        )
    }

    /**
     * Navigates to the action screen when the payments call returns an action. This covers both origins of a payments
     * call, the payment method list and the payment method screen, because both submit through this controller.
     *
     * [CheckoutController.navigation] has no replay, and a payment method that needs no user interaction is submitted
     * immediately after this subscription is set up. [CoroutineStart.UNDISPATCHED] makes sure the subscription is
     * active before that submit happens, so the route cannot be missed.
     */
    private fun observeNavigation(flow: ActiveFlow) {
        flow.coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            flow.controller.navigation.collect { route ->
                when (route) {
                    // Replacing the back stack means going back from the action cancels Drop-in, and makes the stack
                    // identical whether the action came from the list or from the payment method screen.
                    is CheckoutRoute.Action -> {
                        promoteToActiveFlow(flow)
                        navigator.clearAndNavigateTo(ActionNavKey)
                    }
                    else -> Unit
                }
            }
        }
    }

    /**
     * The action screen renders [activeController], so the flow that returned the action has to be the active one.
     * This only does something for the Google Pay standby flow; any other flow is already active.
     */
    private fun promoteToActiveFlow(flow: ActiveFlow) {
        if (activeFlow === flow) return

        activeFlow?.coroutineScope?.cancel()
        activeFlow = flow
        googlePayFlow = null
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
