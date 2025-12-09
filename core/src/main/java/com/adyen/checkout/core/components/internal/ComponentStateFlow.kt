/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface ComponentStateFlow<S : ComponentState, I : Any> : StateFlow<S> {
    fun handleIntent(intent: I)
}

fun <S : ComponentState, I : Any> ComponentStateFlow(
    initialState: S,
    reduce: (S, I) -> S,
    validate: (S) -> S,
    coroutineScope: CoroutineScope,
): ComponentStateFlow<S, I> = ComponentStateFlowImplementation(
    initialState = initialState,
    reduce = reduce,
    validate = validate,
    coroutineScope = coroutineScope,
)

private class ComponentStateFlowImplementation<S : ComponentState, I : Any>(
    initialState: S,
    reduce: (S, I) -> S,
    validate: (S) -> S,
    coroutineScope: CoroutineScope,
) : ComponentStateFlow<S, I> {

    private val intents = Channel<I>(Channel.Factory.BUFFERED)

    private val stateFlow: StateFlow<S> = intents
        .receiveAsFlow()
        .runningFold(initialState) { state, intent ->
            val reduced = reduce(state, intent)
            validate(reduced)
        }
        .stateIn(coroutineScope, SharingStarted.Lazily, initialState)

    override val value: S
        get() = stateFlow.value

    override val replayCache: List<S>
        get() = stateFlow.replayCache

    override suspend fun collect(collector: FlowCollector<S>): Nothing {
        stateFlow.collect(collector)
    }

    override fun handleIntent(intent: I) {
        intents.trySend(intent)
    }
}
