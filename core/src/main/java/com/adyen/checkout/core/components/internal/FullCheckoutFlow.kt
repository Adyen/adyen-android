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
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.CheckoutRoute
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("LongParameterList")
internal class FullCheckoutFlow(
    target: CheckoutTarget,
    context: CheckoutContext,
    callbacks: CheckoutCallbacks,
    coroutineScope: CoroutineScope,
    analyticsManager: AnalyticsManager,
    checkoutConfiguration: CheckoutConfiguration,
    componentParamsBundle: ComponentParamsBundle,
    private val actionHandler: ActionHandler,
) : CheckoutFlow {

    override val paymentComponent: PaymentComponent<*>? = createPaymentComponent(
        target = target,
        context = context,
        callbacks = callbacks,
        coroutineScope = coroutineScope,
        analyticsManager = analyticsManager,
        checkoutConfiguration = checkoutConfiguration,
        componentParamsBundle = componentParamsBundle,
    )

    override val actionComponent: ActionComponent? get() = actionHandler.actionComponent

    override var onNavigate: ((CheckoutRoute) -> Unit)? = null

    init {
        paymentComponent?.eventFlow
            ?.onEach { event ->
                when (event) {
                    is PaymentComponentEvent.Submit -> {
                        paymentComponent.setLoading(true)
                        callbacks.beforeSubmit?.beforeSubmit(event.state)
                        val result = callbacks.onSubmit?.onSubmit(event.state.data)
                        result?.let { handleResult(it) }
                        paymentComponent.setLoading(false)
                    }

                    is PaymentComponentEvent.Error -> {
                        callbacks.onError?.onError(event.error.toCheckoutError())
                    }
                }
            }
            ?.launchIn(coroutineScope)
    }

    // TODO - Ensure we are not handling an action
    override fun submit() {
        paymentComponent?.submit()
    }

    private fun handleResult(checkoutResult: CheckoutResult) {
        when (checkoutResult) {
            is CheckoutResult.Action -> actionHandler.handleAction(checkoutResult.action, onNavigate)
            is CheckoutResult.Error -> {
                // TODO - Handle error state
            }

            is CheckoutResult.Finished -> {
                // TODO - Handle finished state
            }
        }
    }

    private fun createPaymentComponent(
        target: CheckoutTarget,
        context: CheckoutContext,
        callbacks: CheckoutCallbacks,
        coroutineScope: CoroutineScope,
        analyticsManager: AnalyticsManager,
        checkoutConfiguration: CheckoutConfiguration,
        componentParamsBundle: ComponentParamsBundle,
    ): PaymentComponent<*>? {
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
                        checkoutConfiguration = checkoutConfiguration,
                        componentParamsBundle = componentParamsBundle,
                        checkoutCallbacks = callbacks,
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
                        checkoutConfiguration = checkoutConfiguration,
                        componentParamsBundle = componentParamsBundle,
                        checkoutCallbacks = callbacks,
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
