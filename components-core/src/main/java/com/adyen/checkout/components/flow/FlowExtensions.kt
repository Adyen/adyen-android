/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/8/2022.
 */

package com.adyen.checkout.components.flow

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * This method creates a shared flow that produces a single value at a time (buffer = 1), with priority to the latest
 * (BufferOverflow.DROP_OLDEST).
 *
 * Being a shared flow, the values emitted behave as events (as opposed to state) which means if an observer re-observes
 * the flow, they will NOT receive a value that they already collected earlier.
 *
 * Having replay = 1 ensures that an observer that started observing this flow after it has already emitted an event
 * will be able to collect it and will not miss out on any past events.
 */
@Suppress("FunctionName")
fun <T> MutableSingleEventSharedFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 1,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)
