/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.components.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

// TODO docs
class SubmitHandler {

    fun <T : PaymentComponentState<out PaymentMethodDetails>> onSubmit(
        state: T,
        submitChannel: Channel<T>,
        uiEventChannel: Channel<PaymentComponentUiEvent>?,
        uiStateChannel: MutableStateFlow<PaymentComponentUiState>?
    ) {
        when {
            !state.isInputValid -> uiEventChannel?.trySend(PaymentComponentUiEvent.InvalidUI)
            state.isValid -> {
                submitChannel.trySend(state)
                uiStateChannel?.tryEmit(PaymentComponentUiState.Idle)
            }
            !state.isReady -> uiStateChannel?.tryEmit(PaymentComponentUiState.Loading)
            else -> uiStateChannel?.tryEmit(PaymentComponentUiState.Idle)
        }
    }

    fun <T : PaymentComponentState<out PaymentMethodDetails>> onState(
        state: T,
        uiState: PaymentComponentUiState?,
        submitChannel: Channel<T>
    ) {
        if (uiState == PaymentComponentUiState.Loading) {
            if (state.isValid) {
                submitChannel.trySend(state) // TODO add state to submit
            } else {
                // set ui state to idle?
            }
        } else if (state.isValid) { // TODO don't forget is confirmation required
            submitChannel.trySend(state) // TODO add state to submit
        }
    }
}
