/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/6/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.GenericComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.GenericIntent
import com.adyen.checkout.core.components.internal.ui.state.GenericPaymentComponentState
import com.adyen.checkout.core.components.internal.ui.state.GenericViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.GenericDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class GenericPaymentComponent(
    private val analyticsManager: AnalyticsManager,
    private val paymentMethodType: String,
    private val sdkDataProvider: SdkDataProvider,
    private val componentStateValidator: GenericComponentStateValidator,
    componentStateFactory: GenericComponentStateFactory,
    componentStateReducer: GenericComponentStateReducer,
    viewStateProducer: GenericViewStateProducer,
    coroutineScope: CoroutineScope,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
    )

    internal val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        trackRenderEvent()
    }

    private fun trackRenderEvent() {
        val event = GenericEvents.rendered(component = paymentMethodType)
        analyticsManager.trackEvent(event)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        GenericContent(
            viewStateFlow = viewState,
            onSubmitClick = ::submit,
            modifier = modifier,
        )
    }

    override fun submit() {
        // extra safety call, expected to always be valid
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = createPaymentComponentState()
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentState),
            )
        }
    }

    private fun createPaymentComponentState(): GenericPaymentComponentState {
        return GenericPaymentComponentState(
            data = PaymentComponentData(
                paymentMethod = GenericDetails(
                    type = paymentMethodType,
                    sdkData = sdkDataProvider.createEncodedSdkData(),
                    // TODO - Check if we should remove subtype from GenericDetails
                    subtype = null,
                ),
                order = null,
            ),
            isValid = true,
        )
    }

    override fun requiresUserInteraction(): Boolean = false

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(GenericIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() = Unit
}
