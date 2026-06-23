/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.SecondaryScreenComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import com.adyen.checkout.mbway.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.view.MBWayContent
import com.adyen.checkout.mbway.internal.ui.view.MBWaySecondaryContent
import com.adyen.checkout.mbway.internal.ui.view.MBWaySecondaryContentEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class MBWayComponent(
    private val analyticsManager: AnalyticsManager,
    private val sdkDataProvider: SdkDataProvider,
    private val componentStateValidator: MBWayComponentStateValidator,
    componentStateFactory: MBWayComponentStateFactory,
    componentStateReducer: MBWayComponentStateReducer,
    viewStateProducer: MBWayViewStateProducer,
    coroutineScope: CoroutineScope,
) : PaymentComponent,
    SecondaryScreenComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = coroutineScope,
    )

    private val viewState = componentState.viewState(viewStateProducer, coroutineScope)

    init {
        initializeAnalytics()
    }

    private fun initializeAnalytics() {
        analyticsManager.initialize()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        MBWayContent(
            modifier = modifier,
            viewStateFlow = viewState,
            onSubmitClick = ::submit,
            onCountryCodePickerClick = ::onCountryCodePickerClick,
            onIntent = ::onIntent,
        )
    }

    @Composable
    override fun SecondaryContent(identifier: String, modifier: Modifier) {
        MBWaySecondaryContent(
            modifier = modifier,
            identifier = identifier,
            viewState = viewState,
            onIntent = ::onIntent,
            onDismissRequest = { eventChannel.trySend(PaymentComponentEvent.CloseSecondaryScreen) },
        )
    }

    private fun onIntent(intent: MBWayIntent) {
        componentState.handleIntent(intent)
    }

    private fun onCountryCodePickerClick() {
        eventChannel.trySend(
            PaymentComponentEvent.SecondaryScreen(
                identifier = MBWaySecondaryContentEntry.COUNTRY_CODE_PICKER,
            ),
        )
    }

    override fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            val paymentComponentState = componentState.value.toPaymentComponentState(
                sdkData = sdkDataProvider.createEncodedSdkData(),
            )
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentState),
            )
        } else {
            onIntent(MBWayIntent.HighlightValidationErrors)
        }
    }

    override fun requiresUserInteraction(): Boolean = true

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(MBWayIntent.UpdateLoading(isLoading))
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }
}
