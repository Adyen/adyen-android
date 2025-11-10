/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionProvider
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.CheckoutCompositionLocalProvider
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutDisplayStrategy
import com.adyen.checkout.ui.internal.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.Dimensions
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
    private val commonComponentParams: CommonComponentParams,
) {

    private var actionComponent: ActionComponent? = null
    private var actionObservationJob: Job? = null

    private lateinit var backStack: NavBackStack<NavKey>

    @Composable
    fun ViewFactory(
        modifier: Modifier = Modifier,
        localizationProvider: CheckoutLocalizationProvider?,
    ) {
        backStack = rememberNavBackStack(paymentComponent.navigationStartingPoint)
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
                val metadata = when (entry.displayStrategy) {
                    CheckoutDisplayStrategy.INLINE -> emptyMap()
                    CheckoutDisplayStrategy.DIALOG -> DialogSceneStrategy.dialog(
                        DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = false,
                            usePlatformDefaultWidth = false,
                            decorFitsSystemWindows = false,
                        ),
                    )
                }
                NavEntry(key = key, metadata = metadata) {
                    if (entry.displayStrategy == CheckoutDisplayStrategy.DIALOG) {
                        Surface(
                            color = CheckoutThemeProvider.colors.background,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .systemBarsPadding()
                                    .padding(Dimensions.Large),
                            ) {
                                entry.content(backStack)
                            }
                        }
                    } else {
                        Column(modifier) {
                            entry.content(backStack)
                        }
                    }
                }
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

    @Suppress("UNUSED_PARAMETER")
    private fun handleIntent(intent: Intent) {
        // TODO - handle intent with action component
    }
}
