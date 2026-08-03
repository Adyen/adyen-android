/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentStateFlow<C : ComponentState, I : ComponentStateIntent> : StateFlow<C> {
    fun handleIntent(intent: I)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <C : ComponentState, I : ComponentStateIntent> ComponentStateFlow(
    initialState: C,
    reducer: ComponentStateReducer<C, I>,
    validator: ComponentStateValidator<C>,
): ComponentStateFlow<C, I> = ComponentStateFlowImplementation(
    initialState = initialState,
    reducer = reducer,
    validator = validator,
)

// Owns its state directly in a MutableStateFlow rather than folding intents over a cold flow. A cold flow's
// accumulator is torn down by WhileSubscribed once the last collector goes away (e.g. the app is backgrounded for
// longer than the sharing timeout), and nothing replays the intents handled while nobody was collecting - so
// shopper input would silently be discarded. Owning the state directly means there is no subscription whose
// cancellation could lose anything.
//
// This requires reducer.reduce and validator.validate to be pure (no side effects), because MutableStateFlow.update
// can re-invoke its lambda under contention.
private class ComponentStateFlowImplementation<C : ComponentState, I : ComponentStateIntent>(
    initialState: C,
    private val reducer: ComponentStateReducer<C, I>,
    private val validator: ComponentStateValidator<C>,
) : ComponentStateFlow<C, I> {

    // Validated up front: isValid reads the errorMessage that validate() writes, so an unvalidated state reports
    // itself valid no matter what it contains.
    private val state = MutableStateFlow(validator.validate(initialState))

    override val value: C
        get() = state.value

    override val replayCache: List<C>
        get() = state.replayCache

    override suspend fun collect(collector: FlowCollector<C>): Nothing {
        state.collect(collector)
    }

    override fun handleIntent(intent: I) {
        state.update { validator.validate(reducer.reduce(it, intent)) }
    }
}

// Default timeout suggested by the Android team
// https://medium.com/androiddevelopers/things-to-know-about-flows-sharein-and-statein-operators-20e6ccb2bc74
private const val SUBSCRIBE_TIMEOUT_MS = 5_000L

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <C : ComponentState, V : ViewState> ComponentStateFlow<C, *>.viewState(
    producer: ViewStateProducer<C, V>,
    coroutineScope: CoroutineScope,
): StateFlow<V> {
    return this.map(producer::produce)
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT_MS), producer.produce(value))
}
