/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionDelegate
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.PaymentDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentFacilitator(
    private val paymentDelegate: PaymentDelegate<BaseComponentState>,
    private val coroutineScope: CoroutineScope,
    private val componentEventHandler: ComponentEventHandler<BaseComponentState>,
    private val actionProvider: ActionProvider,
) {

    private var actionDelegate by mutableStateOf<ActionDelegate?>(null)

    @Composable
    fun ViewFactory(modifier: Modifier = Modifier) {
        if (actionDelegate != null) {
            actionDelegate?.ViewFactory(modifier)
        } else {
            paymentDelegate.ViewFactory(modifier)
        }
    }

    fun submit() {
        paymentDelegate.submit()
    }

    fun observe(lifecycle: Lifecycle) {
        paymentDelegate.eventFlow
            .flowWithLifecycle(lifecycle)
            .filterNotNull()
            .onEach { event ->
                componentEventHandler.onPaymentComponentEvent(event, ::handleResult)
            }.launchIn(coroutineScope)
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
        actionDelegate = actionProvider.get(
            action = action,
            coroutineScope = coroutineScope,
        )
        // TODO - Adyen log
//        adyenLog(AdyenLogLevel.DEBUG) { "Created delegate of type ${actionDelegate::class.simpleName}" }
    }
}
