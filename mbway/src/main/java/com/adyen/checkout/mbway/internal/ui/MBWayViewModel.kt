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
import com.adyen.checkout.core.components.internal.ui.NewPaymentComponent
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import com.adyen.checkout.mbway.internal.ui.state.toPaymentComponentState

internal class MBWayViewModel(
    // TODO - Add constructor params
//    private val componentParams: ComponentParams,
//    private val analyticsManager: AnalyticsManager,
//    private val sdkDataProvider: SdkDataProvider,
    private val componentStateValidator: MBWayComponentStateValidator,
    componentStateFactory: MBWayComponentStateFactory,
    componentStateReducer: MBWayComponentStateReducer,
    viewStateProducer: MBWayViewStateProducer,
) : ViewModel(), NewPaymentComponent {

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

    override fun validate(): Boolean {
        onIntent(MBWayIntent.HighlightValidationErrors)
        return componentStateValidator.isValid(componentState.value)
    }

    override fun setLoading(isLoading: Boolean) {
        componentState.handleIntent(MBWayIntent.UpdateLoading(isLoading))
    }

    override fun getState(): PaymentComponentState<*> {
        // TODO - Use actual amount and sdkData
        return componentState.value.toPaymentComponentState(
            amount = null,
            sdkData = null,
        )
    }

    fun onIntent(intent: MBWayIntent) {
        componentState.handleIntent(intent)
    }

    override fun onCleared() {
//        analyticsManager.clear(this)
    }
}
