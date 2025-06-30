/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/6/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * This method creates a buffered [Channel] suitable for use in a single time event use case.
 *
 * Being a [Channel], the values emitted behave as events (as opposed to state) which means if an observer re-observes
 * the channel, they will NOT receive a value that they already collected earlier.
 *
 * Another advantage is that the producer can send values to the [Channel] even if no consumer is observing it.
 *
 * In theory we should be able to replace this with [MutableSharedFlow] but it didn't work in practice because the
 * [MutableSharedFlow.tryEmit] method does not work as expected, meaning that emitted values will be silently lost if
 * the flow has no observers at the moment the values are emitted.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> bufferedChannel(): Channel<T> = Channel(Channel.BUFFERED)
