/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

import com.adyen.checkout.components.core.internal.internal.ui.model.TestFieldTransformerRegistry
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DefaultDelegateStateManagerTest {

    private lateinit var stateManager: DefaultDelegateStateManager<TestDelegateState, TestFieldId>

    private var defaultDelegateState = TestDelegateState()
    private val delegateStateFactory = TestDelegateStateFactory(defaultDelegateState)
    private val validationRegistry = TestFieldValidatorRegistry()
    private val stateUpdaterRegistry = TestStateUpdaterRegistry()
    private val transformerRegistry = TestFieldTransformerRegistry<TestFieldId>()

    @BeforeEach
    fun setup() {
        stateUpdaterRegistry.initialize(ComponentFieldDelegateState(value = "", validation = null))
        stateManager = DefaultDelegateStateManager(
            factory = delegateStateFactory,
            validationRegistry = validationRegistry,
            stateUpdaterRegistry = stateUpdaterRegistry,
            transformerRegistry = transformerRegistry,
        )
    }

    @Test
    fun `when state is initialized, then default state should be emitted`() = runTest {
        assertEquals(defaultDelegateState, stateManager.state.value)
    }

    @Test
    fun `when isValid is accessed, then non-validated fields should be validated`() = runTest {
        val fieldState = ComponentFieldDelegateState(value = "test", validation = null)
        stateUpdaterRegistry.updateFieldState(defaultDelegateState, TestFieldId.FIELD_1, fieldState)

        stateManager.isValid

        validationRegistry.assertIsValidated(TestFieldId.FIELD_1)
    }

    @Test
    fun `when updateFieldValue is called and value is not null, then validate is called`() {
        stateManager.updateFieldValue(TestFieldId.FIELD_1, value = "Some value")

        transformerRegistry.assertIsTransformed(TestFieldId.FIELD_1)
        validationRegistry.assertIsValidated(TestFieldId.FIELD_1)
    }

    @Test
    fun `when updateFieldValue is called and value is null, then validate is not called`() {
        stateManager.updateFieldValue(TestFieldId.FIELD_1, value = null)

        transformerRegistry.assertNotTransformed(TestFieldId.FIELD_1)
        validationRegistry.assertNotValidated(TestFieldId.FIELD_1)
    }

    @Test
    fun `when updateFieldValue is called, then field state should be updated`() {
        val initialFieldState = ComponentFieldDelegateState(
            value = "",
            validation = null,
            hasFocus = false,
            shouldHighlightValidationError = false,
        )
        stateUpdaterRegistry.updateFieldState(defaultDelegateState, TestFieldId.FIELD_1, initialFieldState)

        stateManager.updateFieldValue(
            fieldId = TestFieldId.FIELD_1,
            value = "New value",
        )

        val updatedFieldState = stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_1)
        assertEquals("New value", updatedFieldState.value)
        assertEquals(false, updatedFieldState.hasFocus)
    }

    @Test
    fun `when updateFieldFocus is called, then field state should be updated`() {
        val initialFieldState = ComponentFieldDelegateState(
            value = "",
            validation = null,
            hasFocus = false,
            shouldHighlightValidationError = false,
        )
        stateUpdaterRegistry.updateFieldState(defaultDelegateState, TestFieldId.FIELD_1, initialFieldState)

        stateManager.updateFieldFocus(
            fieldId = TestFieldId.FIELD_1,
            hasFocus = true,
        )

        val updatedFieldState = stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_1)
        assertEquals("", updatedFieldState.value)
        assertEquals(true, updatedFieldState.hasFocus)
    }
}
