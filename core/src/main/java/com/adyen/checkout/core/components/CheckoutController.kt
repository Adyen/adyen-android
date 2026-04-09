/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.AnalyticsManagerFactory
import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.core.components.internal.ui.model.ComponentParamsBundle
import com.adyen.checkout.core.error.toCheckoutError
import com.adyen.checkout.core.sessions.internal.model.SessionParams
import com.adyen.checkout.core.sessions.internal.model.SessionParamsFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

fun CheckoutController(
    target: CheckoutTarget,
    context: CheckoutContext,
    callbacks: CheckoutCallbacks,
    // TODO - find a way to not require application context in the controller
    applicationContext: Context,
    coroutineScope: CoroutineScope,
): CheckoutController {
    val checkoutConfiguration: CheckoutConfiguration
    val checkoutAttemptId: String?
    val publicKey: String?
    val componentSessionParams: SessionParams?
    val sessionId: String?

    when (context) {
        is CheckoutContext.Advanced -> {
            checkoutConfiguration = context.checkoutConfiguration
            checkoutAttemptId = context.checkoutAttemptId
            publicKey = context.publicKey
            componentSessionParams = null
            sessionId = null
        }

        is CheckoutContext.Sessions -> {
            checkoutConfiguration = context.checkoutConfiguration
            checkoutAttemptId = context.checkoutAttemptId
            publicKey = context.publicKey
            componentSessionParams = SessionParamsFactory.create(context.checkoutSession)
            sessionId = context.checkoutSession.sessionSetupResponse.id
        }
    }

    val componentParamsBundle = CommonComponentParamsMapper().mapToParams(
        checkoutConfiguration = checkoutConfiguration,
        deviceLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault(),
        dropInOverrideParams = null,
        componentSessionParams = componentSessionParams,
        publicKey = publicKey,
    )

    val analyticsManager = AnalyticsManagerFactory().provide(
        componentParams = componentParamsBundle.commonComponentParams,
        applicationContext = applicationContext,
        // TODO - Analytics: Pass the correct paymentMethod type
        source = AnalyticsSource.PaymentComponent("paymentMethod.type"),
        sessionId = sessionId,
        checkoutAttemptId = checkoutAttemptId,
    )

    return CheckoutController(
        target = target,
        context = context,
        callbacks = callbacks,
        coroutineScope = coroutineScope,
        analyticsManager = analyticsManager,
        checkoutConfiguration = checkoutConfiguration,
        componentParamsBundle = componentParamsBundle,
    )
}

@Suppress("LongParameterList")
class CheckoutController internal constructor(
    private val target: CheckoutTarget,
    private val context: CheckoutContext,
    private val callbacks: CheckoutCallbacks,
    private val coroutineScope: CoroutineScope,
    private val analyticsManager: AnalyticsManager,
    private val checkoutConfiguration: CheckoutConfiguration,
    private val componentParamsBundle: ComponentParamsBundle,
) {

    internal val paymentComponent: PaymentComponent<*>?
    internal var actionComponent: ActionComponent? = null
        private set

    internal var onNavigate: ((CheckoutRoute) -> Unit)? = null

    init {
        // TODO - Move this logic to the factory and into a separate class
        paymentComponent = when (target) {
            is CheckoutTarget.PaymentMethod -> {
                val paymentMethod = getPaymentMethodResponse()?.paymentMethods?.find { it.type == target.type }

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
                val storedPaymentMethod = getPaymentMethodResponse()?.storedPaymentMethods?.find { it.id == target.id }

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

    private fun getPaymentMethodResponse(): PaymentMethods? {
        return when (context) {
            is CheckoutContext.Advanced -> context.paymentMethods
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethods
        }
    }

    private fun handleResult(checkoutResult: CheckoutResult) {
        when (checkoutResult) {
            is CheckoutResult.Action -> handleAction(checkoutResult.action)
            is CheckoutResult.Error -> {
                // TODO - Handle error state
            }

            is CheckoutResult.Finished -> {
                // TODO - Handle finished state
            }
        }
    }

    private fun handleAction(action: Action) {
        val actionComponent = ActionComponentProvider.get(
            action = action,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            checkoutConfiguration = checkoutConfiguration,
            // TODO - Check if we really need saved state handle
            savedStateHandle = @SuppressLint("VisibleForTests") SavedStateHandle(),
            // TODO - Check if session params should be taken into account
            commonComponentParams = componentParamsBundle.commonComponentParams,
        )
        this.actionComponent = actionComponent

        actionComponent.eventFlow
            .onEach { event ->
                when (event) {
                    is ActionComponentEvent.ActionDetails -> {
                        callbacks.onAdditionalDetails?.onAdditionalDetails(event.data)
                    }

                    is ActionComponentEvent.Error -> {
                        callbacks.onError?.onError(event.error.toCheckoutError())
                    }
                }
            }
            .launchIn(coroutineScope)

        actionComponent.handleAction()

        onNavigate?.invoke(CheckoutRoute.Action)
    }

    // TODO - Ensure we are not handling an action
    fun submit() {
        paymentComponent?.submit()
    }
}
