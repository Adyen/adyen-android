/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/3/2025.
 */

package core.adyen.checkout.core.internal.ui.state

import com.adyen.checkout.core.internal.ui.state.model.DelegateFieldState
import com.adyen.checkout.core.internal.ui.state.model.Validation
import com.adyen.checkout.core.internal.ui.state.model.shouldShowValidationError
import com.adyen.checkout.core.internal.ui.state.model.toViewFieldState
import com.adyen.checkout.core.internal.ui.state.model.updateFieldState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DelegateFieldStateTest {

    private lateinit var initialState: DelegateFieldState<String>

    @BeforeEach
    fun setup() {
        initialState = DelegateFieldState(
            value = "initial",
            validation = Validation.Valid,
            hasFocus = false,
            shouldHighlightValidationError = false,
        )
    }

    @Test
    fun `when updateFieldState is called with new values, then field state should update correctly`() {
        val updatedState = initialState.updateFieldState(
            value = "updated",
            validation = Validation.Invalid(123),
            hasFocus = true,
            shouldHighlightValidationError = true,
        )

        assertEquals("updated", updatedState.value)
        assertEquals(Validation.Invalid(123), updatedState.validation)
        assertTrue(updatedState.hasFocus)
        assertTrue(updatedState.shouldHighlightValidationError)
    }

    @Test
    fun `when updateFieldState is called with null values, then original values should be retained`() {
        val updatedState = initialState.updateFieldState(
            value = null,
            validation = null,
            hasFocus = null,
            shouldHighlightValidationError = null,
        )

        assertEquals("initial", updatedState.value)
        assertEquals(Validation.Valid, updatedState.validation)
        assertFalse(updatedState.hasFocus)
        assertFalse(updatedState.shouldHighlightValidationError)
    }

    @Test
    fun `when toViewFieldState is called with valid validation, then errorMessageId should be null`() {
        val viewState = initialState.toViewFieldState()

        assertEquals("initial", viewState.value)
        assertFalse(viewState.hasFocus)
        assertNull(viewState.errorMessageId)
    }

    @Test
    fun `when toViewFieldState is called with invalid validation and shouldHighlightValidationError is true, then errorMessageId should be set`() {
        val updatedState = initialState.copy(
            validation = Validation.Invalid(456),
            shouldHighlightValidationError = true,
        )
        val viewState = updatedState.toViewFieldState()

        assertEquals("initial", viewState.value)
        assertFalse(viewState.hasFocus)
        Assertions.assertEquals(456, viewState.errorMessageId)
    }

    @Test
    fun `when toViewFieldState is called with invalid validation and shouldHighlightValidationError is false, then errorMessageId should be null`() {
        val updatedState = initialState.copy(
            validation = Validation.Invalid(456),
            shouldHighlightValidationError = false,
            hasFocus = true,
        )
        val viewState = updatedState.toViewFieldState()

        assertEquals("initial", viewState.value)
        assertTrue(viewState.hasFocus)
        assertNull(viewState.errorMessageId)
    }

    @Test
    fun `when shouldShowValidationError is called and hasFocus is false, then it should return true`() {
        val updatedState = initialState.copy(
            hasFocus = false,
        )

        assertTrue(updatedState.shouldShowValidationError())
    }

    @Test
    fun `when shouldShowValidationError is called and shouldHighlightValidationError is true, then it should return true`() {
        val updatedState = initialState.copy(
            hasFocus = true,
            shouldHighlightValidationError = true,
        )

        assertTrue(updatedState.shouldShowValidationError())
    }

    @Test
    fun `when shouldShowValidationError is called and hasFocus is true and shouldHighlightValidationError is false, then it should return false`() {
        val updatedState = initialState.copy(
            hasFocus = true,
            shouldHighlightValidationError = false,
        )

        assertFalse(updatedState.shouldShowValidationError())
    }
}
