/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/10/2022.
 */

package com.adyen.checkout.dropin.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.components.channel.bufferedChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class ActionComponentViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val eventsChannel: Channel<ActionComponentEvent> = bufferedChannel()
    val eventsFlow: Flow<ActionComponentEvent> = eventsChannel.receiveAsFlow()

    private var isInitialized: Boolean
        get() = savedStateHandle[IS_INITIALIZED] ?: false
        private set(value) {
            savedStateHandle[IS_INITIALIZED] = value
        }

    init {
        launchAction()
    }

    private fun launchAction() {
        if (isInitialized) return
        isInitialized = true
        eventsChannel.trySend(ActionComponentEvent.HANDLE_ACTION)
    }

    companion object {
        private const val IS_INITIALIZED = "IS_INITIALIZED"
    }
}

enum class ActionComponentEvent {
    HANDLE_ACTION
}
