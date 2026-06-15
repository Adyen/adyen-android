/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.CheckoutPaymentMethodRoute
import com.adyen.checkout.core.components.CheckoutSecondaryRoute
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

internal class FullCheckoutFlow(
    componentRequestDispatcher: SubmittableComponentRequestDispatcher,
    coroutineScope: CoroutineScope,
    override val paymentComponent: PaymentComponent?,
    private val actionHandler: ActionHandler,
) : CheckoutFlow {

    override val actionComponent: ActionComponent? get() = actionHandler.actionComponent

    private val paymentMethodNavigationChannel: Channel<CheckoutPaymentMethodRoute> = bufferedChannel()
    override val paymentMethodNavigation: Flow<CheckoutPaymentMethodRoute> =
        paymentMethodNavigationChannel.receiveAsFlow()

    private val secondaryNavigationChannel: Channel<CheckoutSecondaryRoute> = bufferedChannel()
    override val secondaryNavigation: Flow<CheckoutSecondaryRoute> =
        secondaryNavigationChannel.receiveAsFlow()

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
                        paymentMethodNavigationChannel.trySend(CheckoutPaymentMethodRoute.Secondary(event.identifier))
                    }

                    PaymentComponentEvent.CloseSecondaryScreen -> {
                        secondaryNavigationChannel.trySend(CheckoutSecondaryRoute.PaymentMethod())
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
                paymentMethodNavigationChannel.trySend(CheckoutPaymentMethodRoute.Action())
            }

            is SubmitResult.Completion,
            is SubmitResult.Retry -> {
                // No-op: there is nothing we should do in these cases
            }

            is SubmitResult.PartialPayment -> {
                // TODO - Handle partial payment state
            }
        }
    }
}
