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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.common.localization.internal.helper.LocalizedComponent
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import java.util.Locale

internal class PaymentFacilitator(
    private val paymentComponent: PaymentComponent<BasePaymentComponentState>,
    private val coroutineScope: CoroutineScope,
    private val componentEventHandler: ComponentEventHandler<BasePaymentComponentState>,
    private val actionProvider: ActionProvider,
    private val checkoutController: CheckoutController,
    private val shopperLocale: Locale,
) {

    private var actionComponent by mutableStateOf<ActionComponent?>(null)
    private var actionObservationJob: Job? = null

    private lateinit var backStack: NavBackStack<NavKey>

    @Composable
    fun ViewFactory(modifier: Modifier = Modifier, localizationProvider: CheckoutLocalizationProvider?) {
        backStack = rememberNavBackStack(PaymentNavKey)
        LocalizedComponent(
            locale = shopperLocale,
            localizationProvider = localizationProvider,
        ) {
            NavDisplay(
                backStack = backStack,
                entryProvider = entryProvider {
                    entry<PaymentNavKey> { paymentComponent.ViewFactory(modifier) }
                    entry<ActionNavKey> { actionComponent?.ViewFactory(modifier) }
                },
            )
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

        backStack.clear()
        backStack.add(ActionNavKey)

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

@Serializable
private data object PaymentNavKey : NavKey

@Serializable
private data object ActionNavKey : NavKey

class Test : NavEntryDecorator<NavKey>(
    onPop = {},
    decorate = { entry -> entry.Content() }
)
