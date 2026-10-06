/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionData
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.action.internal.RestorableActionComponent
import com.adyen.checkout.core.action.internal.ReturningActionComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ActionHandler
@Suppress("LongParameterList")
constructor(
    private val componentRequestDispatcher: ComponentRequestDispatcher,
    private val coroutineScope: CoroutineScope,
    private val analyticsManager: AnalyticsManager,
    private val params: CheckoutParams,
    private val savedStateHandle: SavedStateHandle,
    private val stateStore: CheckoutFlowStateStore,
    private val onAction: (ActionData) -> Unit,
) {

    var actionComponent: ActionComponent? = null
        private set

    private var job: Job? = null

    fun handleAction(action: Action) {
        val actionComponent = createActionComponent(action) ?: return
        stateStore.save(CheckoutFlowPhase.HandlingAction(action))
        observe(actionComponent)

        onAction(ActionData(action.type))

        actionComponent.handleAction()
    }

    /**
     * Resumes [action] after process death. The merchant was already notified about the action before, so `onAction`
     * is not called again.
     */
    fun restoreAction(action: Action) {
        val actionComponent = createActionComponent(action) ?: return
        if (actionComponent !is RestorableActionComponent) {
            this.actionComponent = null
            stateStore.clear()
            val error = CheckoutError(
                code = CheckoutError.ErrorCode.GENERIC,
                message = "The '${action.type}' action cannot be resumed, because the app was stopped while it was " +
                    "being handled. Check the payment status on your server.",
            )
            componentRequestDispatcher.failure(error)
            return
        }
        observe(actionComponent)

        actionComponent.restoreAction()
    }

    private fun createActionComponent(action: Action): ActionComponent? {
        job?.cancel()
        this.actionComponent = null

        val actionComponent = try {
            ActionComponentProvider.get(
                action = action,
                coroutineScope = coroutineScope,
                analyticsManager = analyticsManager,
                params = params,
                savedStateHandle = savedStateHandle,
            )
        } catch (e: InternalCheckoutError) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Could not create an action component for action '${action.type}'" }
            componentRequestDispatcher.failure(e.toCheckoutError())
            return null
        }
        this.actionComponent = actionComponent
        return actionComponent
    }

    private fun observe(actionComponent: ActionComponent) {
        job = actionComponent.eventFlow
            .onEach { event ->
                when (event) {
                    is ActionComponentEvent.ActionDetails -> {
                        val result = componentRequestDispatcher.additionalDetails(event.data)
                        handleResult(result)
                    }

                    is ActionComponentEvent.Error -> {
                        componentRequestDispatcher.failure(event.error.toCheckoutError())
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    private fun handleResult(result: AdditionalDetailsResult) {
        when (result) {
            is AdditionalDetailsResult.Completion -> {
                stateStore.clear()
                componentRequestDispatcher.complete(CheckoutResultCode(result.resultCode))
            }
        }
    }

    fun handleReturn(intent: Intent) {
        (actionComponent as? ReturningActionComponent)?.handleReturn(intent) ?: run {
            adyenLog(AdyenLogLevel.WARN) {
                "handleReturn called but actionComponent is null or not a ReturningActionComponent"
            }
        }
    }
}
