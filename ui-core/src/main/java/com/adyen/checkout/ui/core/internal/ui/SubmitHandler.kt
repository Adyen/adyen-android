/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/11/2022.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SubmitHandler<ComponentStateT : PaymentComponentState<*>>(
    override val savedStateHandle: SavedStateHandle,
) : SavedStateHandleContainer {

    private var isInteractionBlocked: Boolean? by SavedStateHandleProperty(IS_INTERACTION_BLOCKED)

    private val submitChannel: Channel<ComponentStateT> = bufferedChannel()
    val submitFlow: Flow<ComponentStateT> = submitChannel.receiveAsFlow()

    private val _uiStateFlow = MutableStateFlow<PaymentComponentUIState>(PaymentComponentUIState.Idle)
    val uiStateFlow: Flow<PaymentComponentUIState> = _uiStateFlow

    private val uiEventChannel: Channel<PaymentComponentUIEvent> = bufferedChannel()
    val uiEventFlow: Flow<PaymentComponentUIEvent> = uiEventChannel.receiveAsFlow()

    fun initialize(
        coroutineScope: CoroutineScope,
        componentStateFlow: Flow<ComponentStateT>,
    ) {
        isInteractionBlocked?.let {
            setInteractionBlocked(it)
        }
        componentStateFlow.onEach { state ->
            val uiState = _uiStateFlow.value
            if (uiState == PaymentComponentUIState.PendingSubmit) {
                if (state.isValid) {
                    submitChannel.trySend(state)
                }
                resetUIState()
            }
        }.launchIn(coroutineScope)
    }

    fun onSubmit(state: ComponentStateT) {
        when {
            !state.isInputValid -> uiEventChannel.trySend(PaymentComponentUIEvent.InvalidUI)
            state.isValid -> {
                uiEventChannel.trySend(PaymentComponentUIEvent.HideKeyboard)
                submitChannel.trySend(state)
            }
            !state.isReady -> _uiStateFlow.tryEmit(PaymentComponentUIState.PendingSubmit)
            else -> resetUIState() // unreachable state
        }
    }

    fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        this.isInteractionBlocked = isInteractionBlocked
        resetUIState()
    }

    private fun resetUIState() {
        val uiState = when (isInteractionBlocked) {
            null -> PaymentComponentUIState.Idle
            true -> PaymentComponentUIState.Blocked
            false -> PaymentComponentUIState.Idle
        }
        _uiStateFlow.tryEmit(uiState)
    }

    companion object {
        @VisibleForTesting
        internal const val IS_INTERACTION_BLOCKED = "IS_INTERACTION_BLOCKED"
    }
}
