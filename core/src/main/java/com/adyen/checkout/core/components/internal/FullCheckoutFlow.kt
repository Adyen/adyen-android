/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutPaymentMethodRoute
import com.adyen.checkout.core.components.CheckoutSecondaryRoute
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class FullCheckoutFlow(
    target: CheckoutTarget,
    context: CheckoutContext,
    callbacks: CheckoutCallbacks,
    componentRequestDispatcher: ComponentRequestDispatcher,
    coroutineScope: CoroutineScope,
    analyticsManager: AnalyticsManager,
    params: CheckoutParams,
    private val actionHandler: ActionHandler,
) : CheckoutFlow {

    // TODO - Inject paymentComponent in constructor
    override val paymentComponent: PaymentComponent? = createPaymentComponent(
        target = target,
        context = context,
        callbacks = callbacks,
        coroutineScope = coroutineScope,
        analyticsManager = analyticsManager,
        params = params,
    )

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
                        componentRequestDispatcher.error(event.error.toCheckoutError())
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

    private fun createPaymentComponent(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        params: CheckoutParams,
    ): PaymentComponent? {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> {
                val paymentMethod = context.getPaymentMethodResponse()
                    ?.paymentMethods
                    ?.find { it.type == target.type }

                if (paymentMethod == null) {
                    null
                } else {
                    PaymentMethodProvider.getPaymentComponent(
                        paymentMethod = paymentMethod,
                        coroutineScope = coroutineScope,
                        analyticsManager = analyticsManager,
                        params = params,
                        additionalCallbacks = callbacks.additionalCallbacks,
                    )
                }
            }

            is CheckoutTarget.StoredPaymentMethod -> {
                val storedPaymentMethod = context.getPaymentMethodResponse()
                    ?.storedPaymentMethods
                    ?.find { it.id == target.id }

                if (storedPaymentMethod == null) {
                    null
                } else {
                    PaymentMethodProvider.getStoredPaymentComponent(
                        storedPaymentMethod = storedPaymentMethod,
                        coroutineScope = coroutineScope,
                        analyticsManager = analyticsManager,
                        params = params,
                    )
                }
            }

            else -> null
        }
    }

    private fun CheckoutContext.getPaymentMethodResponse(): PaymentMethods? {
        return when (this) {
            is CheckoutContext.Advanced -> paymentMethods
            is CheckoutContext.Sessions -> checkoutSession.sessionSetupResponse.paymentMethods
        }
    }
}
