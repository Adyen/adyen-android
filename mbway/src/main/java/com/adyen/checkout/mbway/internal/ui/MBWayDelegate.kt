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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.OrderRequest
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.EventDelegate
import com.adyen.checkout.core.components.internal.ui.PaymentDelegate
import com.adyen.checkout.core.components.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.core.components.internal.ui.state.DelegateStateManager
import com.adyen.checkout.core.components.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformerRegistry
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.mbway.internal.ui.model.toViewState
import com.adyen.checkout.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.view.MbWayComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@Suppress("UnusedPrivateProperty")
internal class MBWayDelegate(
    private val coroutineScope: CoroutineScope,
    private val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    // TODO - Order to be passed later
    private val order: OrderRequest? = null,
    private val transformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    private val stateManager: DelegateStateManager<MBWayDelegateState, MBWayFieldId>,
) : PaymentDelegate<MBWayComponentState>,
    FieldChangeListener<MBWayFieldId>,
    EventDelegate<MBWayComponentState> {

    private val eventChannel = bufferedChannel<PaymentComponentEvent<MBWayComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<MBWayComponentState>> =
        eventChannel.receiveAsFlow()

    private val componentStateFlow: StateFlow<MBWayComponentState> by lazy {
        val toComponentState: (MBWayDelegateState) -> MBWayComponentState = { delegateState ->
            delegateState.toComponentState(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                fieldTransformerRegistry = transformerRegistry,
                order = order,
                amount = componentParams.amount,
            )
        }
        stateManager.state
            .map(toComponentState)
            .stateIn(
                coroutineScope,
                SharingStarted.Lazily,
                toComponentState(stateManager.state.value),
            )
    }

    private val viewStateFlow: StateFlow<MBWayViewState> by lazy {
        stateManager.state
            .map(MBWayDelegateState::toViewState)
            .stateIn(coroutineScope, SharingStarted.Lazily, stateManager.state.value.toViewState())
    }

    override fun submit() {
        if (stateManager.isValid) {
            eventChannel.trySend(
                PaymentComponentEvent.Submit(componentStateFlow.value),
            )
        } else {
            stateManager.highlightAllFieldValidationErrors()
        }
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        val viewState = viewStateFlow.collectAsStateWithLifecycle()

        MbWayComponent(
            viewState.value,
            fieldChangeListener = this,
        )
    }

    override fun <T> onFieldValueChanged(
        fieldId: MBWayFieldId,
        value: T
    ) = stateManager.updateFieldValue(fieldId, value)

    override fun onFieldFocusChanged(
        fieldId: MBWayFieldId,
        hasFocus: Boolean
    ) = stateManager.updateFieldFocus(fieldId, hasFocus)
}
