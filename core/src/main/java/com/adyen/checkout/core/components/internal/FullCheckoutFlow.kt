/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean

internal class FullCheckoutFlow(
    private val componentRequestDispatcher: SubmittableComponentRequestDispatcher,
    coroutineScope: CoroutineScope,
    override val paymentComponent: PaymentComponent,
    private val actionHandler: ActionHandler,
    private val stateStore: CheckoutFlowStateStore,
) : CheckoutFlow {

    override val actionComponent: ActionComponent? get() = actionHandler.actionComponent

    private val canSubmit = AtomicBoolean(true)

    init {
        restoreSavedState()

        paymentComponent.eventFlow
            .onEach { event ->
                when (event) {
                    is PaymentComponentEvent.Submit -> {
                        if (!canSubmit.compareAndSet(true, false)) {
                            adyenLog(AdyenLogLevel.WARN) {
                                "Component was already submitted, ignoring subsequent submit request"
                            }
                            return@onEach
                        }
                        paymentComponent.setLoading(true)
                        stateStore.save(CheckoutFlowPhase.Submitted)
                        val result = componentRequestDispatcher.submit(event.state.data)
                        handleResult(result)
                    }

                    is PaymentComponentEvent.Error -> {
                        componentRequestDispatcher.failure(event.error.toCheckoutError())
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    private fun restoreSavedState() {
        when (val phase = stateStore.restore()) {
            CheckoutFlowPhase.Submitted -> failWithUnknownOutcome()
            is CheckoutFlowPhase.HandlingAction -> {
                canSubmit.set(false)
                actionHandler.restoreAction(phase.action)
            }

            CheckoutFlowPhase.Input, null -> Unit
        }
    }

    /**
     * The process died after the payment was submitted and before a response was received, so the payment outcome is
     * unknown. Submitting again could result in a double payment, so the flow fails instead. The saved state is
     * cleared, so that the failure is only reported once.
     */
    private fun failWithUnknownOutcome() {
        canSubmit.set(false)
        stateStore.clear()
        val error = CheckoutError(
            code = CheckoutError.ErrorCode.GENERIC,
            message = "The payment outcome is unknown, because the app was stopped while the payment was being " +
                "submitted. Check the payment status on your server.",
        )
        componentRequestDispatcher.failure(error)
    }

    override fun submit() {
        paymentComponent.submit()
    }

    override fun requiresUserInteraction(): Boolean =
        actionComponent == null && paymentComponent.requiresUserInteraction()

    private fun handleResult(submitResult: SubmitResult) {
        when (submitResult) {
            is SubmitResult.Action -> {
                actionHandler.handleAction(submitResult.action)
            }

            is SubmitResult.Completion -> {
                stateStore.clear()
                componentRequestDispatcher.complete(CheckoutResultCode(submitResult.resultCode))
            }

            is SubmitResult.Retry -> {
                stateStore.save(CheckoutFlowPhase.Input)
                canSubmit.set(true)
                paymentComponent.setLoading(false)
            }

            is SubmitResult.PartialPayment -> {
                // TODO - Handle partial payment state
            }
        }
    }

    override fun handleReturn(intent: Intent) {
        actionHandler.handleReturn(intent)
    }
}
