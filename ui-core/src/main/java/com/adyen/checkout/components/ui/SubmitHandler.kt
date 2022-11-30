/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.components.ui

import com.adyen.checkout.components.PaymentComponentState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

// TODO docs
class SubmitHandler {

    fun onSubmit(
        state: PaymentComponentState<*>,
        submitChannel: Channel<Unit>,
        uiEventChannel: Channel<PaymentComponentUiEvent>,
        uiStateChannel: MutableStateFlow<PaymentComponentUiState>
    ) {
        when {
            !state.isInputValid -> uiEventChannel.trySend(PaymentComponentUiEvent.InvalidUI)
            state.isValid -> {
                submitChannel.trySend(Unit)
                uiStateChannel.tryEmit(PaymentComponentUiState.Idle)
            }
            !state.isReady -> uiStateChannel.tryEmit(PaymentComponentUiState.Loading)
            else -> uiStateChannel.tryEmit(PaymentComponentUiState.Idle)
        }
    }

    fun onState(
        state: PaymentComponentState<*>,
        uiState: PaymentComponentUiState,
        submitChannel: Channel<Unit>
    ) {
        if (uiState == PaymentComponentUiState.Loading) {
            if (state.isValid) {
                submitChannel.trySend(Unit) // TODO add state to submit
            } else {
                // set ui state to idle?
            }
        } else if (state.isValid) { // TODO don't forget is confirmation required
            submitChannel.trySend(Unit) // TODO add state to submit
        }
    }
}
