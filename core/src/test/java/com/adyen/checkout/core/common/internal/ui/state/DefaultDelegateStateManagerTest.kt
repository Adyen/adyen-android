/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/4/2025.
 */

package com.adyen.checkout.core.common.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.DefaultDelegateStateManager
import com.adyen.checkout.core.components.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.components.internal.ui.state.model.Validation
import com.adyen.checkout.test.ui.state.TestFieldTransformerRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
        stateUpdaterRegistry.initialize(DelegateFieldState(value = "", validation = null))
        stateManager = DefaultDelegateStateManager(
            factory = delegateStateFactory,
            validationRegistry = validationRegistry,
            stateUpdaterRegistry = stateUpdaterRegistry,
            transformerRegistry = transformerRegistry,
        )
    }

    @Test
    fun `when state is initialized, then default state should be emitted`() {
        assertEquals(defaultDelegateState, stateManager.state.value)
    }

    @Test
    fun `when isValid is accessed, then non-validated fields should be validated`() {
        val fieldState = DelegateFieldState(value = "test", validation = null)
        stateUpdaterRegistry.updateFieldState(defaultDelegateState, TestFieldId.FIELD_1, fieldState)

        stateManager.isValid

        validationRegistry.assertIsValidated(TestFieldId.FIELD_1)
    }

    @Test
    fun `when updateState is called, then the state is updated and fields are revalidated`() {
        val stateUpdaterRegistry = TestStateUpdaterRegistry().apply {
            initialize(DelegateFieldState(value = "", validation = Validation.Valid))
        }
        val stateManager = DefaultDelegateStateManager(
            factory = TestDelegateStateFactory(defaultDelegateState),
            validationRegistry = validationRegistry,
            stateUpdaterRegistry = stateUpdaterRegistry,
            transformerRegistry = transformerRegistry,
        )
        val newState = TestDelegateState(isValid = false)

        stateManager.updateState { newState }

        assertEquals(newState, stateManager.state.value)
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
        val initialFieldState = DelegateFieldState(
            value = "",
            validation = null,
            hasFocus = false,
            shouldHighlightValidationError = false,
        )
        stateUpdaterRegistry.updateFieldState(
            defaultDelegateState,
            TestFieldId.FIELD_1,
            initialFieldState,
        )

        stateManager.updateFieldValue(
            fieldId = TestFieldId.FIELD_1,
            value = "New value",
        )

        val updatedFieldState =
            stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_1)
        assertEquals("New value", updatedFieldState.value)
        assertEquals(false, updatedFieldState.hasFocus)
    }

    @Test
    fun `when updateFieldFocus is called, then field state should be updated`() {
        val initialFieldState = DelegateFieldState(
            value = "",
            validation = null,
            hasFocus = false,
            shouldHighlightValidationError = false,
        )
        stateUpdaterRegistry.updateFieldState(
            defaultDelegateState,
            TestFieldId.FIELD_1,
            initialFieldState,
        )

        stateManager.updateFieldFocus(
            fieldId = TestFieldId.FIELD_1,
            hasFocus = true,
        )

        val updatedFieldState =
            stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_1)
        assertEquals("", updatedFieldState.value)
        assertEquals(true, updatedFieldState.hasFocus)
    }

    @Test
    fun `when highlightAllFieldValidationErrors is called, then invalid fields should be highlighted and focused`() {
        val field1State = DelegateFieldState(value = "invalid", validation = Validation.Invalid(1))
        val field2State = DelegateFieldState(value = "valid", validation = Validation.Valid)
        val field3State = DelegateFieldState(value = "invalid", validation = Validation.Invalid(2))

        stateUpdaterRegistry.updateFieldState(
            defaultDelegateState,
            TestFieldId.FIELD_1,
            field1State,
        )
        stateUpdaterRegistry.updateFieldState(
            defaultDelegateState,
            TestFieldId.FIELD_2,
            field2State,
        )
        stateUpdaterRegistry.updateFieldState(
            defaultDelegateState,
            TestFieldId.FIELD_3,
            field3State,
        )

        stateManager.highlightAllFieldValidationErrors()

        val updatedField1State =
            stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_1)
        val updatedField2State =
            stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_2)
        val updatedField3State =
            stateUpdaterRegistry.getFieldState<String>(defaultDelegateState, TestFieldId.FIELD_3)
        assertTrue(updatedField1State.hasFocus)
        assertTrue(updatedField1State.shouldHighlightValidationError)
        assertFalse(updatedField2State.hasFocus)
        assertTrue(updatedField2State.shouldHighlightValidationError)
        assertFalse(updatedField3State.hasFocus)
        assertTrue(updatedField3State.shouldHighlightValidationError)
    }
}
