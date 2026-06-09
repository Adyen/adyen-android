/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionComponentProvider
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.error.toCheckoutError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ActionHandler(
    private val componentRequestDispatcher: ComponentRequestDispatcher,
    private val coroutineScope: CoroutineScope,
    private val analyticsManager: AnalyticsManager,
    private val params: CheckoutParams,
) {

    var actionComponent: ActionComponent? = null
        private set

    private var job: Job? = null

    fun handleAction(action: Action) {
        job?.cancel()

        val actionComponent = ActionComponentProvider.get(
            action = action,
            coroutineScope = coroutineScope,
            analyticsManager = analyticsManager,
            params = params,
            // TODO - Check if we really need saved state handle
            savedStateHandle = @SuppressLint("VisibleForTests") SavedStateHandle(),
        )
        this.actionComponent = actionComponent

        job = actionComponent.eventFlow
            .onEach { event ->
                when (event) {
                    is ActionComponentEvent.ActionDetails -> {
                        componentRequestDispatcher.additionalDetails(event.data)
                    }

                    is ActionComponentEvent.Error -> {
                        componentRequestDispatcher.error(event.error.toCheckoutError())
                    }
                }
            }
            .launchIn(coroutineScope)

        actionComponent.handleAction()
    }
}
