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
import com.adyen.checkout.core.components.NewCheckoutController
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFlow
import com.adyen.checkout.core.components.internal.ui.state.viewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateFactory
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateReducer
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentStateValidator
import com.adyen.checkout.mbway.internal.ui.state.MBWayIntent
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewStateProducer
import kotlinx.coroutines.launch

internal class MBWayViewModel(
    private val controller: NewCheckoutController,
    // TODO - Add constructor params
//    private val componentParams: ComponentParams,
//    private val analyticsManager: AnalyticsManager,
//    private val sdkDataProvider: SdkDataProvider,
    private val componentStateValidator: MBWayComponentStateValidator,
    componentStateFactory: MBWayComponentStateFactory,
    componentStateReducer: MBWayComponentStateReducer,
    viewStateProducer: MBWayViewStateProducer,
) : ViewModel() {

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
            viewModelScope.launch {
                componentState.handleIntent(MBWayIntent.UpdateLoading(true))
                controller.submit()
                componentState.handleIntent(MBWayIntent.UpdateLoading(false))
            }
        } else {
            onIntent(MBWayIntent.HighlightValidationErrors)
        }
    }

    fun onIntent(intent: MBWayIntent) {
        componentState.handleIntent(intent)
    }

    override fun onCleared() {
//        analyticsManager.clear(this)
    }
}
