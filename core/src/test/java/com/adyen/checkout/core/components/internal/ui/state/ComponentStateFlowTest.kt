/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/7/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import com.adyen.checkout.core.common.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Characterisation tests: they describe what [ComponentStateFlow] does today, quirks included, rather than what it
 * should do. They exist as a safety net for the code the form field refactor rewrites.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ComponentStateFlowTest {

    private val reducer = TestReducer()

    @Test
    fun `when the flow is collected before any intent, then the initial state is emitted already validated`() =
        runTest {
            // GIVEN
            val stateFlow = createStateFlow()

            // WHEN
            val states = stateFlow.test(testScheduler)
            testScheduler.runCurrent()

            // THEN
            assertEquals(listOf(VALIDATED_INITIAL_STATE), states.values)
        }

    @Test
    fun `when no intent has been handled, then the value is the validated initial state`() = runTest {
        // GIVEN
        val stateFlow = createStateFlow()

        // WHEN
        val value = stateFlow.value

        // THEN
        assertEquals(VALIDATED_INITIAL_STATE, value)
    }

    @Test
    fun `when an intent is handled, then the reduced state is validated before it is emitted`() = runTest {
        // GIVEN
        val stateFlow = createStateFlow()
        val states = stateFlow.test(testScheduler)

        // WHEN
        stateFlow.handleIntent(TestIntent("a"))
        testScheduler.runCurrent()

        // THEN
        assertEquals(TestState(value = "a", validationCount = 2), states.latestValue)
    }

    @Test
    fun `when several intents are handled, then each reduction continues from the previously validated state`() =
        runTest {
            // GIVEN
            val stateFlow = createStateFlow()
            val states = stateFlow.test(testScheduler)

            // WHEN
            stateFlow.handleIntent(TestIntent("a"))
            stateFlow.handleIntent(TestIntent("b"))
            testScheduler.runCurrent()

            // THEN
            val expectedReduced = listOf(VALIDATED_INITIAL_STATE, TestState(value = "a", validationCount = 2))
            assertEquals(expectedReduced, reducer.reducedStates)
            assertEquals(TestState(value = "ab", validationCount = 3), states.latestValue)
        }

    @Test
    fun `when nothing is collecting, then a handled intent is still applied`() = runTest {
        // GIVEN
        val stateFlow = createStateFlow()

        // WHEN
        stateFlow.handleIntent(TestIntent("a"))
        testScheduler.runCurrent()

        // THEN
        assertEquals(TestState(value = "a", validationCount = 2), stateFlow.value)
    }

    @Test
    fun `when collection starts, then intents handled before it are applied`() = runTest {
        // GIVEN
        val stateFlow = createStateFlow()
        stateFlow.handleIntent(TestIntent("a"))

        // WHEN
        stateFlow.test(testScheduler)
        testScheduler.runCurrent()

        // THEN
        assertEquals(TestState(value = "a", validationCount = 2), stateFlow.value)
    }

    @Test
    fun `when collection stops for longer than the sharing timeout, then the state survives`() =
        runTest {
            // GIVEN
            val stateFlow = createStateFlow()
            val states = stateFlow.test(testScheduler)
            stateFlow.handleIntent(TestIntent("a"))
            testScheduler.runCurrent()
            assertEquals(TestState(value = "a", validationCount = 2), stateFlow.value)

            // WHEN
            states.cancel()
            testScheduler.advanceTimeBy(SHARING_TIMEOUT_MS + 1)
            stateFlow.test(testScheduler)
            testScheduler.runCurrent()

            // THEN
            assertEquals(TestState(value = "a", validationCount = 2), stateFlow.value)
        }

    @Test
    fun `when a view state is created, then its value is produced from the current component state`() = runTest {
        // GIVEN
        val stateFlow = createStateFlow()

        // WHEN
        val viewStateFlow = stateFlow.viewState(TestViewStateProducer, backgroundScope)

        // THEN
        assertEquals(TestViewState(value = ""), viewStateFlow.value)
    }

    @Test
    fun `when an intent is handled, then the view state follows the component state`() = runTest {
        // GIVEN
        val stateFlow = createStateFlow()
        val viewStates = stateFlow.viewState(TestViewStateProducer, backgroundScope).test(testScheduler)

        // WHEN
        stateFlow.handleIntent(TestIntent("a"))
        testScheduler.runCurrent()

        // THEN
        assertEquals(TestViewState(value = "a"), viewStates.latestValue)
    }

    private fun createStateFlow() = ComponentStateFlow(
        initialState = INITIAL_STATE,
        reducer = reducer,
        validator = TestValidator,
    )

    private data class TestState(
        val value: String = "",
        val validationCount: Int = 0,
    ) : ComponentState

    private data class TestIntent(val append: String) : ComponentStateIntent

    private class TestReducer : ComponentStateReducer<TestState, TestIntent> {

        val reducedStates = mutableListOf<TestState>()

        override fun reduce(state: TestState, intent: TestIntent): TestState {
            reducedStates += state
            return state.copy(value = state.value + intent.append)
        }
    }

    private object TestValidator : ComponentStateValidator<TestState> {

        override fun validate(state: TestState) = state.copy(validationCount = state.validationCount + 1)

        override fun isValid(state: TestState) = true
    }

    private data class TestViewState(val value: String) : ViewState

    private object TestViewStateProducer : ViewStateProducer<TestState, TestViewState> {

        override fun produce(state: TestState) = TestViewState(state.value)
    }

    companion object {

        private val INITIAL_STATE = TestState()

        // ComponentStateFlow validates the initial state before it is stored, so this is what it actually emits.
        private val VALIDATED_INITIAL_STATE = TestState(validationCount = 1)

        // Mirrors the private SUBSCRIBE_TIMEOUT_MS in ComponentStateFlow.
        private const val SHARING_TIMEOUT_MS = 5_000L
    }
}
