/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.core.PaymentMethodTypes
import com.adyen.checkout.core.data.PaymentComponentData
import com.adyen.checkout.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.internal.ui.EventDelegate
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.core.internal.ui.state.DefaultDelegateStateManager
import com.adyen.checkout.core.internal.ui.state.DelegateStateManager
import com.adyen.checkout.core.internal.ui.state.FieldChangeListener
import com.adyen.checkout.core.internal.util.bufferedChannel
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayStateUpdaterRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayTransformerRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayValidatorRegistry
import com.adyen.checkout.core.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.core.mbway.internal.ui.model.toViewState
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId
import com.adyen.checkout.core.mbway.internal.ui.view.MbWayComponent
import com.adyen.checkout.core.paymentmethod.MBWayPaymentMethod
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
    private val componentParams: ButtonComponentParams
) : PaymentDelegate<MBWayComponentState>,
    FieldChangeListener<MBWayFieldId>,
    EventDelegate<MBWayComponentState> {

    private val stateManager: DelegateStateManager<MBWayDelegateState, MBWayFieldId> =
        createStateManager()

    private val eventChannel = bufferedChannel<PaymentComponentEvent<MBWayComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<MBWayComponentState>> = eventChannel.receiveAsFlow()

    // TODO Here we can add a componentStateFlow and generate it, like we generate the
    //  viewStateFlow. The `chore/state_management` branch has its implementation.

    private val viewStateFlow: StateFlow<MBWayViewState> by lazy {
        stateManager.state
            .map(MBWayDelegateState::toViewState)
            .stateIn(coroutineScope, SharingStarted.Lazily, stateManager.state.value.toViewState())
    }

    override fun submit() {
        if (stateManager.isValid) {
            eventChannel.trySend(
                PaymentComponentEvent.Submit(
                    // TODO - Adjust this logic when componentStateFlow is moved to here
                    //  from `chore/state_management` branch.
                    MBWayComponentState(
                        PaymentComponentData(
                            MBWayPaymentMethod(
                                type = PaymentMethodTypes.MB_WAY,

                                // TODO - Pass this after getting it in the Checkout initialization (COSDK-443)
                                checkoutAttemptId = null,
                                telephoneNumber = makePhoneNumber(),
                            ),
                            null,
                            componentParams.amount,
                        ),
                    ),
                ),
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

    private fun createStateManager(): DelegateStateManager<MBWayDelegateState, MBWayFieldId> {
        val transformerRegistry = MBWayTransformerRegistry()

        val delegateStateFactory = MBWayDelegateStateFactory()

        return DefaultDelegateStateManager(
            factory = delegateStateFactory,
            validationRegistry = MBWayValidatorRegistry(),
            stateUpdaterRegistry = MBWayStateUpdaterRegistry(),
            transformerRegistry = transformerRegistry,
        )
    }

    // TODO - Remove this logic when componentStateFlow is moved to here from `chore/state_management` branch.
    private fun makePhoneNumber(): String {
        val state = stateManager.state.value
        return state.countryCodeFieldState.value + state.localPhoneNumberFieldState.value
    }
}
