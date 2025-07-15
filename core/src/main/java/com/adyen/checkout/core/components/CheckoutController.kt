/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/7/2025.
 */

package com.adyen.checkout.core.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.adyen.checkout.core.action.data.Action
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// TODO - KDocs
class CheckoutController {

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    internal val events: SharedFlow<Event> = _events.asSharedFlow()

    fun submit() {
        _events.tryEmit(Event.Submit)
    }

    fun handleAction(action: Action) {
        _events.tryEmit(Event.HandleAction(action))
    }

    fun handleIntent(intent: Intent) {
        _events.tryEmit(Event.HandleIntent(intent))
    }

    internal sealed class Event {
        data object Submit : Event()
        data class HandleAction(val action: Action) : Event()
        data class HandleIntent(val intent: Intent) : Event()
    }
}

@Composable
fun rememberCheckoutController() = remember { CheckoutController() }
