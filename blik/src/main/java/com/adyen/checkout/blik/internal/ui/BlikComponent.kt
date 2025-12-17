/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.blik.BlikMainNavigationKey
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateFactory
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateReducer
import com.adyen.checkout.blik.internal.ui.state.BlikComponentStateValidator
import com.adyen.checkout.blik.internal.ui.state.BlikIntent
import com.adyen.checkout.blik.internal.ui.state.BlikPaymentComponentState
import com.adyen.checkout.blik.internal.ui.state.BlikViewStateProducer
import com.adyen.checkout.blik.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.blik.internal.ui.view.BlikComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class BlikComponent(
    private val componentParams: ComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val componentStateValidator: BlikComponentStateValidator,
    componentStateFactory: BlikComponentStateFactory,
    componentStateReducer: BlikComponentStateReducer,
    viewStateProducer: BlikViewStateProducer,
    coroutineScope: CoroutineScope,
) : PaymentComponent<BlikPaymentComponentState> {

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        BlikNavKey to CheckoutNavEntry(BlikNavKey, BlikMainNavigationKey) { backStack -> MainScreen(backStack) },
    )

    override val navigationStartingPoint: NavKey = BlikNavKey

    private val eventChannel = bufferedChannel<PaymentComponentEvent<BlikPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<BlikPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = componentState.value.toPaymentComponentState(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                amount = componentParams.amount,
            )
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentState),
            )
        } else {
            onIntent(BlikIntent.HighlightValidationErrors)
        }
    }

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(BlikIntent.UpdateLoading(isLoading))
    }

    private fun onIntent(intent: BlikIntent) {
        componentState.handleIntent(intent)
    }

    @Composable
    @Suppress("UnusedParameter")
    private fun MainScreen(backStack: NavBackStack<NavKey>) {
        val viewState by viewState.collectAsStateWithLifecycle()

        BlikComponent(
            viewState = viewState,
            onSubmitClick = ::submit,
            onIntent = ::onIntent,
        )
    }
}
