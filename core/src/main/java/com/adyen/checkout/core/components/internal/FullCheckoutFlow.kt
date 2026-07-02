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
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class FullCheckoutFlow(
    private val componentRequestDispatcher: SubmittableComponentRequestDispatcher,
    coroutineScope: CoroutineScope,
    override val paymentComponent: PaymentComponent?,
    private val actionHandler: ActionHandler,
) : CheckoutFlow {

    override val actionComponent: ActionComponent? get() = actionHandler.actionComponent

    private val navigationFlow = MutableSharedFlow<CheckoutRoute>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val navigation: Flow<CheckoutRoute> = navigationFlow.asSharedFlow()

    init {
        paymentComponent?.eventFlow
            ?.onEach { event ->
                when (event) {
                    is PaymentComponentEvent.Submit -> {
                        paymentComponent.setLoading(true)
                        val result = componentRequestDispatcher.submit(event.state.data)
                        handleResult(result)
                        paymentComponent.setLoading(false)
                    }

                    is PaymentComponentEvent.Error -> {
                        componentRequestDispatcher.failure(event.error.toCheckoutError())
                    }

                    is PaymentComponentEvent.SecondaryScreen -> {
                        navigationFlow.tryEmit(CheckoutRoute.Secondary(event.identifier))
                    }

                    PaymentComponentEvent.CloseSecondaryScreen -> {
                        navigationFlow.tryEmit(CheckoutRoute.PaymentMethod())
                    }
                }
            }
            ?.launchIn(coroutineScope)
    }

    // TODO - Ensure we are not handling an action
    override fun submit() {
        paymentComponent?.submit()
    }

    override fun requiresUserInteraction(): Boolean =
        actionComponent == null && paymentComponent?.requiresUserInteraction() == true

    private fun handleResult(submitResult: SubmitResult) {
        when (submitResult) {
            is SubmitResult.Action -> {
                actionHandler.handleAction(submitResult.action)
                navigationFlow.tryEmit(CheckoutRoute.Action())
            }

            is SubmitResult.Completion -> {
                componentRequestDispatcher.complete(CheckoutResultCode(submitResult.resultCode))
            }

            is SubmitResult.Retry -> {
                // No-op: there is nothing we should do in these cases
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
