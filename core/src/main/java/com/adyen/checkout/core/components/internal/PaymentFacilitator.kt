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
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.CoroutineScope
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

    @Composable
    fun ViewFactory(modifier: Modifier = Modifier) {
        if (actionComponent != null) {
            actionComponent?.ViewFactory(modifier)
        } else {
            paymentComponent.ViewFactory(modifier)
        }
    }

    fun observe(lifecycle: Lifecycle) {
        paymentComponent.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                val result = componentEventHandler.onPaymentComponentEvent(event)
                handleResult(result)
            }.launchIn(coroutineScope)

        checkoutController.events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    CheckoutController.Event.Submit -> submit()
                    is CheckoutController.Event.HandleAction -> handleAction(event.action)
                    is CheckoutController.Event.HandleIntent -> handleIntent(event.intent)
                }
            }
            .launchIn(coroutineScope)
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

    private fun submit() {
        // TODO - what if we are handling an action?
        paymentComponent.submit()
    }

    private fun handleAction(action: Action) {
        actionComponent = actionProvider.get(
            action = action,
            coroutineScope = coroutineScope,
        )
        // TODO - Adyen log
//        adyenLog(AdyenLogLevel.DEBUG) { "Created component of type ${actionComponent::class.simpleName}" }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleIntent(intent: Intent) {
        // TODO - handle intent with action component
    }
}
