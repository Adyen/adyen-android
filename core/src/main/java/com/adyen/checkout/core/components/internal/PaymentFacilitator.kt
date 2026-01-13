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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.exception.ComponentError
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.IntentHandlingComponent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.toNavEntry
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class PaymentFacilitator(
    private val paymentComponent: PaymentComponent<BasePaymentComponentState>,
    private val coroutineScope: CoroutineScope,
    private val componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    private val actionProvider: ActionProvider,
    private val checkoutController: CheckoutController,
    private val commonComponentParams: CommonComponentParams,
) {

    private var actionComponent: ActionComponent? = null
    private var actionObservationJob: Job? = null

    private val backStack = NavBackStack(mutableStateListOf(paymentComponent.navigationStartingPoint))

    @Composable
    fun ViewFactory(
        modifier: Modifier,
        localizationProvider: CheckoutLocalizationProvider?,
        navigationProvider: CheckoutNavigationProvider?,
    ) {
        CheckoutCompositionLocalProvider(
            locale = commonComponentParams.shopperLocale,
            localizationProvider = localizationProvider,
            environment = commonComponentParams.environment,
        ) {
            NavDisplay(
                backStack = backStack,
                sceneStrategy = DialogSceneStrategy(),
            ) { key ->
                val entries = paymentComponent.navigation + actionComponent?.navigation.orEmpty()
                val entry = entries[key] ?: error("Unknown key: $key")
                val properties = navigationProvider?.provide(entry.publicKey)
                entry.toNavEntry(modifier, backStack, properties)
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

    fun onCleared() = paymentComponent.onCleared()

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

        backStack.clear()
        backStack.add(actionComponent.navigationStartingPoint)

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

    private fun handleIntent(intent: Intent) {
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
}
