/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.createLocalizedContext
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentFacilitator(
    private val paymentComponent: PaymentComponent<BasePaymentComponentState>,
    private val coroutineScope: CoroutineScope,
    private val componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    private val actionProvider: ActionProvider,
    private val checkoutController: CheckoutController,
) {

    private var actionComponent by mutableStateOf<ActionComponent?>(null)
    private var actionObservationJob: Job? = null

    @Composable
    fun ViewFactory(modifier: Modifier = Modifier) {
        val actionComponent = this.actionComponent
        // TODO - Find an alternative for getting shopper locale without the need to
        //  expose component params from components
        val activeComponentParams = actionComponent?.componentParams ?: paymentComponent.componentParams
        val localizedContext = LocalContext.current.createLocalizedContext(activeComponentParams.shopperLocale)
        CompositionLocalProvider(
            LocalContext provides localizedContext,
        ) {
            if (actionComponent != null) {
                actionComponent.ViewFactory(modifier)
            } else {
                paymentComponent.ViewFactory(modifier)
            }
        }
    }

    fun observe(lifecycle: Lifecycle) {
        paymentComponent.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                paymentComponent.setLoading(true)
                val result = componentEventHandler.onPaymentComponentEvent(event)
                paymentComponent.setLoading(false)
                handleResult(result, lifecycle)
            }.launchIn(coroutineScope)

        checkoutController.events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    CheckoutController.Event.Submit -> submit()
                    is CheckoutController.Event.HandleAction -> handleAction(event.action, lifecycle)
                    is CheckoutController.Event.HandleIntent -> handleIntent(event.intent)
                }
            }
            .launchIn(coroutineScope)
    }

    private fun handleResult(checkoutResult: CheckoutResult, lifecycle: Lifecycle) {
        when (checkoutResult) {
            is CheckoutResult.Action -> handleAction(checkoutResult.action, lifecycle)
            is CheckoutResult.Error -> {
                // TODO - Handle error state
            }

            is CheckoutResult.Finished -> {
                // TODO - Handle finished state
            }
        }
    }

    private fun submit() {
        // TODO - what if we are handling an action?
        paymentComponent.submit()
    }

    private fun handleAction(action: Action, lifecycle: Lifecycle) {
        // In case handleAction() is called twice, we cancel the old observation job
        actionObservationJob?.cancel()

        val actionComponent = actionProvider.get(
            action = action,
            coroutineScope = coroutineScope,
        )
        this.actionComponent = actionComponent

        actionObservationJob = actionComponent.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                val result = componentEventHandler.onActionComponentEvent(event)
                handleResult(result, lifecycle)
            }.launchIn(coroutineScope)

        actionComponent.handleAction()

        adyenLog(AdyenLogLevel.DEBUG) { "Created component of type ${actionComponent::class.simpleName}" }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleIntent(intent: Intent) {
        // TODO - handle intent with action component
    }
}
