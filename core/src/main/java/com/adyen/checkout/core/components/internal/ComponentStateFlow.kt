/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentStateFlow<C : ComponentState, I : Any> : StateFlow<C> {
    fun handleIntent(intent: I)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <C : ComponentState, I : Any> ComponentStateFlow(
    initialState: C,
    reducer: ComponentStateReducer<C, I>,
    validator: ComponentStateValidator<C>,
    coroutineScope: CoroutineScope,
): ComponentStateFlow<C, I> = ComponentStateFlowImplementation(
    initialState = initialState,
    reducer = reducer,
    validator = validator,
    coroutineScope = coroutineScope,
)

private class ComponentStateFlowImplementation<C : ComponentState, I : Any>(
    initialState: C,
    reducer: ComponentStateReducer<C, I>,
    validator: ComponentStateValidator<C>,
    coroutineScope: CoroutineScope,
) : ComponentStateFlow<C, I> {

    private val intents = Channel<I>(Channel.Factory.BUFFERED)

    private val stateFlow: StateFlow<C> = intents
        .receiveAsFlow()
        .runningFold(initialState) { state, intent ->
            val reduced = reducer.reduce(state, intent)
            validator.validate(reduced)
        }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), initialState)

    override val value: C
        get() = stateFlow.value

    override val replayCache: List<C>
        get() = stateFlow.replayCache

    override suspend fun collect(collector: FlowCollector<C>): Nothing {
        stateFlow.collect(collector)
    }

    override fun handleIntent(intent: I) {
        intents.trySend(intent)
    }
}

private const val SUBSCRIBE_TIMEOUT_MS = 500L

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <C : ComponentState, V : ViewState> ComponentStateFlow<C, *>.viewState(
    producer: ViewStateProducer<C, V>,
    coroutineScope: CoroutineScope,
): StateFlow<V> {
    return this.map(producer::produce)
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), producer.produce(value))
}
