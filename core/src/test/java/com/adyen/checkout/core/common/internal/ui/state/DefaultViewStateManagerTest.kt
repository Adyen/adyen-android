/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.common.internal.ui.state

import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.internal.ui.state.DefaultViewStateManager
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.ViewStateFactory
import com.adyen.checkout.core.components.internal.ui.state.ViewStateValidator
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class DefaultViewStateManagerTest(
    @Mock private val viewStateFactory: ViewStateFactory<TestViewState>,
) {

    private val viewStateValidator = TestViewStateValidator()
    private lateinit var stateManager: DefaultViewStateManager<TestViewState>

    @Test
    fun `when initialized, then default state should be set`() = runTest {
        val initialViewState = TestViewState("initial")
        initStateManager(initialViewState)

        val actual = stateManager.state.test(testScheduler).latestValue

        assertEquals(initialViewState, actual)
    }

    @Test
    fun `when updating the state, then the new state should be updated and validated`() = runTest {
        val initialViewState = TestViewState("initial", false)
        initStateManager(initialViewState)
        val viewStateFlow = stateManager.state.test(testScheduler)

        stateManager.update { copy(value = "updated") }

        val actual = viewStateFlow.latestValue
        assertEquals("updated", actual.value)
        assertTrue(actual.didValidate)
    }

    @Test
    fun `when highlighting all validation, then the new state should be reflecting that`() = runTest {
        val initialViewState = TestViewState("initial")
        initStateManager(initialViewState)
        val viewStateFlow = stateManager.state.test(testScheduler)

        stateManager.highlightAllFieldValidationErrors()

        val actual = viewStateFlow.latestValue
        assertTrue(actual.showError)
    }

    private fun initStateManager(initialState: TestViewState) {
        whenever(viewStateFactory.createDefaultViewState()) doReturn initialState
        stateManager = DefaultViewStateManager(
            factory = viewStateFactory,
            validator = viewStateValidator,
        )
    }

    data class TestViewState(
        val value: String,
        val didValidate: Boolean = false,
        val isValid: Boolean = true,
        val showError: Boolean = false,
    ) : ViewState

    class TestViewStateValidator : ViewStateValidator<TestViewState> {

        override fun validate(viewState: TestViewState): TestViewState {
            return viewState.copy(didValidate = true)
        }

        override fun isValid(viewState: TestViewState): Boolean {
            return viewState.isValid
        }

        override fun highlightAllValidationErrors(viewState: TestViewState): TestViewState {
            return viewState.copy(showError = true)
        }
    }
}
