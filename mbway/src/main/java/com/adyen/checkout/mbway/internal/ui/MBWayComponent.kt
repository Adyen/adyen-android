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
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateManager
import com.adyen.checkout.core.components.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformerRegistry
import com.adyen.checkout.mbway.internal.ui.state.MBWayComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.state.MBWayPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.MBWayViewState
import com.adyen.checkout.mbway.internal.ui.state.toPaymentComponentState
import com.adyen.checkout.mbway.internal.ui.state.toViewState
import com.adyen.checkout.mbway.internal.ui.view.MbWayComponent
import com.adyen.checkout.ui.internal.ComponentScaffold
import com.adyen.checkout.ui.internal.PayButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@Suppress("UnusedPrivateProperty")
internal class MBWayComponent(
    private val coroutineScope: CoroutineScope,
    private val componentParams: ComponentParams,
    private val analyticsManager: AnalyticsManager,
    // TODO - Order to be passed later
    private val order: OrderRequest? = null,
    private val transformerRegistry: FieldTransformerRegistry<MBWayFieldId>,
    private val stateManager: ComponentStateManager<MBWayComponentState, MBWayFieldId>,
) : PaymentComponent<MBWayPaymentComponentState>,
    FieldChangeListener<MBWayFieldId> {

    private val eventChannel = bufferedChannel<PaymentComponentEvent<MBWayPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<MBWayPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val paymentComponentStateFlow: StateFlow<MBWayPaymentComponentState> by lazy {
        val toPaymentComponentState: (MBWayComponentState) -> MBWayPaymentComponentState = { componentState ->
            componentState.toPaymentComponentState(
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                fieldTransformerRegistry = transformerRegistry,
                order = order,
                amount = componentParams.amount,
            )
        }
        stateManager.state
            .map(toPaymentComponentState)
            .stateIn(
                coroutineScope,
                SharingStarted.Lazily,
                toPaymentComponentState(stateManager.state.value),
            )
    }

    private val viewStateFlow: StateFlow<MBWayViewState> by lazy {
        stateManager.state
            .map(MBWayComponentState::toViewState)
            .stateIn(coroutineScope, SharingStarted.Lazily, stateManager.state.value.toViewState())
    }

    override fun submit() {
        if (stateManager.isValid) {
            eventChannel.trySend(
                PaymentComponentEvent.Submit(paymentComponentStateFlow.value),
            )
        } else {
            stateManager.highlightAllFieldValidationErrors()
        }
    }

    @Composable
    override fun ViewFactory(modifier: Modifier, onButtonClick: () -> Unit) {
        val viewState = viewStateFlow.collectAsStateWithLifecycle()

        ComponentScaffold(
            modifier = modifier,
            footer = {
                PayButton(onClick = onButtonClick)
            },
        ) {
            MbWayComponent(
                viewState = viewState.value,
                fieldChangeListener = this,
            )
        }
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
