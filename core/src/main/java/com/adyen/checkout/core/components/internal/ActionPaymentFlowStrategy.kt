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
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.exception.ComponentError
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.IntentHandlingComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class ActionPaymentFlowStrategy(
    private val actionComponent: ActionComponent,
    private val componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
) : PaymentFlowStrategy {

    override val navigationStartingPoint: NavKey = actionComponent.navigationStartingPoint

    override val navigationEntries: Map<NavKey, CheckoutNavEntry>
        get() = actionComponent.navigation

    override fun observe(lifecycle: Lifecycle, coroutineScope: CoroutineScope, onResult: (CheckoutResult) -> Unit) {
        actionComponent.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                val result = componentEventHandler.onActionComponentEvent(event)
                onResult(result)
            }
            .launchIn(coroutineScope)

        actionComponent.handleAction()
    }

    override fun submit() = Unit

    override fun handleAction(
        action: Action,
        lifecycle: Lifecycle,
        coroutineScope: CoroutineScope,
        onActionComponentCreated: (NavKey) -> Unit,
        onResult: (CheckoutResult) -> Unit
    ) = Unit

    override fun handleIntent(intent: Intent, coroutineScope: CoroutineScope) {
        if (actionComponent !is IntentHandlingComponent) {
            adyenLog(AdyenLogLevel.DEBUG) {
                "Action component ${actionComponent.javaClass.simpleName} is not type of IntentHandlingComponent"
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
        // TODO - Should we clear the action component?
    }
}
