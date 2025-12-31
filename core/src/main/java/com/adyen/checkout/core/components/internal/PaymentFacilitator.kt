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
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.toNavEntry
import com.adyen.checkout.core.components.navigation.CheckoutNavigationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class PaymentFacilitator(
    private val paymentFlowStrategy: PaymentFlowStrategy,
    private val coroutineScope: CoroutineScope,
    private val checkoutController: CheckoutController,
    private val commonComponentParams: CommonComponentParams,
) {

    private val backStack = NavBackStack(mutableStateListOf(paymentFlowStrategy.navigationStartingPoint))

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
                // TODO - Should we propagate an error to the merchant instead of throwing?
                val entry = paymentFlowStrategy.navigationEntries[key] ?: error("Unknown key: $key")
                val properties = navigationProvider?.provide(entry.publicKey)
                entry.toNavEntry(modifier, backStack, properties)
            }
        }
    }

    fun observe(lifecycle: Lifecycle) {
        paymentFlowStrategy.observe(
            lifecycle = lifecycle,
            coroutineScope = coroutineScope,
            onResult = { result ->
                handleResult(result, lifecycle)
            },
        )

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
        paymentFlowStrategy.submit()
    }

    private fun handleAction(action: Action, lifecycle: Lifecycle) {
        paymentFlowStrategy.handleAction(
            action = action,
            lifecycle = lifecycle,
            coroutineScope = coroutineScope,
            onActionComponentCreated = { navKey ->
                backStack.clear()
                backStack.add(navKey)
            },
            onResult = { result ->
                handleResult(result, lifecycle)
            },
        )
    }

    private fun handleIntent(intent: Intent) {
        paymentFlowStrategy.handleIntent(intent, coroutineScope)
    }

    fun onCleared() = paymentFlowStrategy.onCleared()
}
