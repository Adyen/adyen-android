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
class SubmitHandlerOld {
    // override val savedStateHandle: SavedStateHandle = SavedStateHandle()
    // : SavedStateHandleContainer {

    private var isInteractionBlocked: Boolean? = null // by SavedStateHandleProperty(IS_INTERACTION_BLOCKED)

    // TODO sessions: move all submit channels and flows here and mirror them in the delegates
    // private val submitChannel: Channel<CardComponentState> = bufferedChannel()
    // private val _uiStateFlow = MutableStateFlow<PaymentComponentUIState>(PaymentComponentUIState.Idle)

    fun <T : PaymentComponentState<out PaymentMethodDetails>> initialize(
        coroutineScope: CoroutineScope,
        componentStateFlow: Flow<T>,
        submitChannel: Channel<T>,
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>,
    ) {
        isInteractionBlocked?.let {
            setInteractionBlocked(uiStateFlow, it)
        }
        componentStateFlow.onEach { state ->
            val uiState = uiStateFlow.value
            if (uiState == PaymentComponentUIState.PendingSubmit) {
                if (state.isValid) {
                    submit(submitChannel, state, uiStateFlow)
                } else {
                    emitUIState(uiStateFlow, PaymentComponentUIState.Idle)
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
            state.isValid -> submit(submitChannel, state, uiStateFlow)
            !state.isReady -> emitUIState(uiStateFlow, PaymentComponentUIState.PendingSubmit)
            else -> emitUIState(uiStateFlow, PaymentComponentUIState.Idle)
        }
    }

    private fun <T : PaymentComponentState<out PaymentMethodDetails>> submit(
        submitChannel: Channel<T>,
        state: T,
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>,
    ) {
        submitChannel.trySend(state)
        emitUIState(uiStateFlow, PaymentComponentUIState.Blocked)
    }

    private fun emitUIState(
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>,
        uiState: PaymentComponentUIState,
    ) {
        if (isInteractionBlocked != null) return
        uiStateFlow.tryEmit(uiState)
    }

    fun setInteractionBlocked(
        uiStateFlow: MutableStateFlow<PaymentComponentUIState>, // remove after uiStateFlow becomes a property
        isInteractionBlocked: Boolean,
    ) {
        this.isInteractionBlocked = isInteractionBlocked
        val uiState = if (isInteractionBlocked) {
            PaymentComponentUIState.Blocked
        } else {
            PaymentComponentUIState.Idle
        }
        uiStateFlow.tryEmit(uiState)
    }

    companion object {
        private const val IS_INTERACTION_BLOCKED = "IS_INTERACTION_BLOCKED"
    }
}
