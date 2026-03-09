/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import com.adyen.checkout.mbway.internal.ui.state.toPaymentComponentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class MBWayViewModel(
    // TODO - Add constructor params
//    private val componentParams: ComponentParams,
//    private val analyticsManager: AnalyticsManager,
//    private val sdkDataProvider: SdkDataProvider,
    private val componentStateValidator: MBWayComponentStateValidator,
    componentStateFactory: MBWayComponentStateFactory,
    componentStateReducer: MBWayComponentStateReducer,
    viewStateProducer: MBWayViewStateProducer,
) : ViewModel() {

    private val eventChannel = bufferedChannel<PaymentComponentEvent<MBWayPaymentComponentState>>()
    val eventFlow: Flow<PaymentComponentEvent<MBWayPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val componentState = ComponentStateFlow(
        initialState = componentStateFactory.createInitialState(),
        reducer = componentStateReducer,
        validator = componentStateValidator,
        coroutineScope = viewModelScope,
    )

    val viewState = componentState.viewState(viewStateProducer, viewModelScope)

    init {
        initializeAnalytics()
    }

    private fun initializeAnalytics() {
//        analyticsManager.initialize(this, viewModelScope)
    }

    fun submit() {
        if (componentStateValidator.isValid(componentState.value)) {
            // TODO - pass correct values
            val paymentComponentState = componentState.value.toPaymentComponentState(
                amount = null,
                sdkData = "",
            )
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentState),
            )
        } else {
            onIntent(MBWayIntent.HighlightValidationErrors)
        }
    }

    fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(MBWayIntent.UpdateLoading(isLoading))
    }

    fun onIntent(intent: MBWayIntent) {
        componentState.handleIntent(intent)
    }

    override fun onCleared() {
//        analyticsManager.clear(this)
    }
}
