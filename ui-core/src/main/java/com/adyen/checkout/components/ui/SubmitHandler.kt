/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.components.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// TODO docs
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SubmitHandler {

    // TODO sessions: move all submit channels and flows here and mirror them in the delegates
    // private val submitChannel: Channel<CardComponentState> = bufferedChannel()

    fun <T : PaymentComponentState<out PaymentMethodDetails>> initialize(
        coroutineScope: CoroutineScope,
        componentStateFlow: Flow<T>,
        submitChannel: Channel<T>,
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>,
    ) {
        componentStateFlow.onEach { state ->
            val uiState = uiStateFlow.value
            if (uiState == PaymentComponentUIState.PendingSubmit) {
                if (state.isValid) {
                    submit(submitChannel, state, uiStateFlow)
                } else {
                    uiStateFlow.tryEmit(PaymentComponentUIState.Idle)
                }
            }
        }.launchIn(coroutineScope)
    }

    fun <T : PaymentComponentState<out PaymentMethodDetails>> onSubmit(
        state: T,
        submitChannel: Channel<T>,
        uiEventChannel: Channel<PaymentComponentUIEvent>,
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>
    ) {
        when {
            !state.isInputValid -> uiEventChannel.trySend(PaymentComponentUIEvent.InvalidUI)
            state.isValid -> {
                submit(submitChannel, state, uiStateFlow)
            }
            !state.isReady -> uiStateFlow.tryEmit(PaymentComponentUIState.PendingSubmit)
            else -> uiStateFlow.tryEmit(PaymentComponentUIState.Idle)
        }
    }

    private fun <T : PaymentComponentState<out PaymentMethodDetails>> submit(
        submitChannel: Channel<T>,
        state: T,
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>,
    ) {
        submitChannel.trySend(state)
        uiStateFlow.tryEmit(PaymentComponentUIState.Blocked)
    }
}
