/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 31/12/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.exception.ComponentError
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.IntentHandlingComponent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class DefaultPaymentFlowStrategy(
    private val paymentComponent: PaymentComponent<BasePaymentComponentState>,
    private val componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    private val actionProvider: ActionProvider,
) : PaymentFlowStrategy {

    private var actionComponent: ActionComponent? = null
    private var actionObservationJob: Job? = null

    override val navigationStartingPoint: NavKey = paymentComponent.navigationStartingPoint

    override val navigationEntries: Map<NavKey, CheckoutNavEntry>
        get() = paymentComponent.navigation + actionComponent?.navigation.orEmpty()

    override fun observe(
        lifecycle: Lifecycle,
        coroutineScope: CoroutineScope,
        onResult: (CheckoutResult) -> Unit,
    ) {
        paymentComponent.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                paymentComponent.setLoading(true)
                val result = componentEventHandler.onPaymentComponentEvent(event)
                paymentComponent.setLoading(false)
                onResult(result)
            }
            .launchIn(coroutineScope)
    }

    override fun submit() {
        paymentComponent.submit()
    }

    override fun handleAction(
        action: Action,
        lifecycle: Lifecycle,
        coroutineScope: CoroutineScope,
        onActionComponentCreated: (NavKey) -> Unit,
        onResult: (CheckoutResult) -> Unit,
    ) {
        // In case handleAction() is called twice, we cancel the old observation job
        actionObservationJob?.cancel()

        val actionComponent = actionProvider.get(
            action = action,
            coroutineScope = coroutineScope,
        )
        this.actionComponent = actionComponent

        onActionComponentCreated(actionComponent.navigationStartingPoint)

        actionObservationJob = actionComponent.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                val result = componentEventHandler.onActionComponentEvent(event)
                onResult(result)
            }
            .launchIn(coroutineScope)

        actionComponent.handleAction()

        adyenLog(AdyenLogLevel.DEBUG) { "Created component of type ${actionComponent::class.simpleName}" }
    }

    override fun handleIntent(intent: Intent, coroutineScope: CoroutineScope) {
        val actionComponent = actionComponent
        if (actionComponent !is IntentHandlingComponent) {
            adyenLog(AdyenLogLevel.DEBUG) {
                "Action component ${actionComponent?.javaClass?.simpleName} is not type of IntentHandlingComponent"
            }
            coroutineScope.launch {
                componentEventHandler.onActionComponentEvent(
                    event = ActionComponentEvent.Error(
                        // TODO - Error propagation. Should this be an implementation error?
                        error = ComponentError(
                            message = "Action component does not implement IntentHandlingComponent",
                        ),
                    ),
                )
            }
            return
        }
        actionComponent.handleIntent(intent)
    }

    override fun onCleared() {
        paymentComponent.onCleared()
        // TODO - should we clear the action component?
    }
}
