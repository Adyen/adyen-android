/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayIntent
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import com.adyen.checkout.googlepay.internal.ui.state.toPaymentComponentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class GooglePayComponent(
    private val analyticsManager: AnalyticsManager,
    // TODO - Will be used to build the Google Pay sheet request and availability check.
    @Suppress("UnusedPrivateProperty", "UnusedPrivateMember")
    private val componentParams: GooglePayComponentParams,
    private val sdkDataProvider: SdkDataProvider,
    private val paymentMethodType: String,
    private val componentStateValidator: GooglePayComponentStateValidator,
    componentStateFactory: GooglePayComponentStateFactory,
    componentStateReducer: GooglePayComponentStateReducer,
    viewStateProducer: GooglePayViewStateProducer,
    coroutineScope: CoroutineScope,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    internal val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        analyticsManager.initialize(this, coroutineScope)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        // TODO - Render the Google Pay button and launch the payment sheet.
        Box(modifier = modifier)
    }

    override fun submit() {
        // TODO - Launch the Google Pay sheet and handle the result before emitting.
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = componentState.value.toPaymentComponentState(
                paymentMethodType = paymentMethodType,
                sdkDataProvider = sdkDataProvider,
            )
            eventChannel.trySend(PaymentComponentEvent.Submit(paymentComponentState))
        }
    }

    override fun requiresUserInteraction(): Boolean = true

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(GooglePayIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }

    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.GOOGLE_PAY, PaymentMethodTypes.GOOGLE_PAY_LEGACY)
    }
}
