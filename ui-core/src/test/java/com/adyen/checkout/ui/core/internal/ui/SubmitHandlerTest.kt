/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/2/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.ui.core.TestComponentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SubmitHandlerTest {

    lateinit var submitHandler: SubmitHandler<PaymentComponentState<*>>

    @Nested
    @DisplayName("when initializing and ")
    inner class InitializeTest {

        @Test
        fun `isInteractionBlocked is set, then UI state should be emitted`() = runTest {
            val savedStateHandle = SavedStateHandle(mapOf(SubmitHandler.IS_INTERACTION_BLOCKED to true))
            submitHandler = createSubmitHandler(savedStateHandle)

            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), flowOf())

            assertEquals(PaymentComponentUIState.Blocked, submitHandler.uiStateFlow.first())
        }

        @Test
        fun `UI state is PendingSubmit and component state is valid, then component state should be emitted`() =
            runTest {
                val componentStateFlow = MutableStateFlow(createComponentState())
                submitHandler = createSubmitHandler()

                submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), componentStateFlow)

                // Make sure the UI state is PaymentComponentUIState.PendingSubmit
                submitHandler.onSubmit(createComponentState(isInputValid = true, isReady = false))

                // Simulate changing the component state to valid
                val expected = createComponentState(isInputValid = true, isReady = true)
                componentStateFlow.tryEmit(expected)

                assertEquals(expected, submitHandler.submitFlow.first())
            }

        @Test
        fun `component state flow gets updated, then StateUpdated state should be emitted`() = runTest {
            val componentStateFlow = MutableStateFlow(createComponentState())
            submitHandler = createSubmitHandler()
            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), componentStateFlow)

            componentStateFlow.tryEmit(createComponentState())

            assertEquals(PaymentComponentUIEvent.StateUpdated, submitHandler.uiEventFlow.first())
        }
    }

    @Nested
    @DisplayName("when onSubmit is called and ")
    inner class OnSubmitTest {

        @Test
        fun `input is invalid, then InvalidUI state should be emitted`() = runTest {
            submitHandler = createSubmitHandler()
            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), flowOf())

            submitHandler.onSubmit(createComponentState(isInputValid = false))

            assertEquals(PaymentComponentUIEvent.InvalidUI, submitHandler.uiEventFlow.first())
        }

        @Test
        fun `component state is valid, then component state should be emitted`() = runTest {
            submitHandler = createSubmitHandler()
            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), flowOf())

            val componentState = createComponentState(isInputValid = true, isReady = true)
            submitHandler.onSubmit(componentState)

            assertEquals(componentState, submitHandler.submitFlow.first())
        }

        @Test
        fun `component state is not ready, then PendingSubmit state should be emitted`() = runTest {
            submitHandler = createSubmitHandler()
            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), flowOf())

            val componentState = createComponentState(isInputValid = true, isReady = false)
            submitHandler.onSubmit(componentState)

            assertEquals(PaymentComponentUIState.PendingSubmit, submitHandler.uiStateFlow.first())
        }
    }

    @Nested
    @DisplayName("when setInteractionBlocked is called with")
    inner class InteractionBlockedTest {

        @Test
        fun `true, then UI state should be blocked`() = runTest {
            submitHandler = createSubmitHandler()
            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), flowOf())

            submitHandler.setInteractionBlocked(true)

            assertEquals(PaymentComponentUIState.Blocked, submitHandler.uiStateFlow.first())
        }

        @Test
        fun `false, then UI state should be idle`() = runTest {
            submitHandler = createSubmitHandler()
            submitHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()), flowOf())

            submitHandler.setInteractionBlocked(false)

            assertEquals(PaymentComponentUIState.Idle, submitHandler.uiStateFlow.first())
        }
    }

    private fun createSubmitHandler(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = SubmitHandler<PaymentComponentState<*>>(savedStateHandle)

    private fun createComponentState(
        isInputValid: Boolean = false,
        isReady: Boolean = false,
    ) = TestComponentState(
        data = PaymentComponentData(null, null, null),
        isInputValid = isInputValid,
        isReady = isReady,
    )
}
