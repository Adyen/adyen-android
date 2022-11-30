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
import com.adyen.checkout.components.bundle.SavedStateHandleContainer
import com.adyen.checkout.components.bundle.SavedStateHandleProperty
import com.adyen.checkout.components.channel.bufferedChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class ActionComponentViewModel(
    override val savedStateHandle: SavedStateHandle
) : ViewModel(), SavedStateHandleContainer {

    private val eventsChannel: Channel<ActionComponentFragmentEvent> = bufferedChannel()
    val eventsFlow: Flow<ActionComponentFragmentEvent> = eventsChannel.receiveAsFlow()

    private var isInitialized: Boolean? by SavedStateHandleProperty(IS_INITIALIZED)

    init {
        launchAction()
    }

    private fun launchAction() {
        if (isInitialized == true) return
        isInitialized = true
        eventsChannel.trySend(ActionComponentFragmentEvent.HANDLE_ACTION)
    }

    companion object {
        private const val IS_INITIALIZED = "IS_INITIALIZED"
    }
}

enum class ActionComponentFragmentEvent {
    HANDLE_ACTION
}
