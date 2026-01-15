/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BlikComponentStateValidatorTest {

    private lateinit var validator: BlikComponentStateValidator

    @BeforeEach
    fun beforeEach() {
        validator = BlikComponentStateValidator()
    }

    @Nested
    inner class ValidateTest {

        @Test
        fun `when blik code is valid 6 digits, then validate should return state without error message`() {
            val state = createState(blikCode = VALID_BLIK_CODE)

            val actual = validator.validate(state)

            assertNull(actual.blikCode.errorMessage)
        }

        @Test
        fun `when blik code is empty, then validate should return state with error message`() {
            val state = createState(blikCode = "")

            val actual = validator.validate(state)

            assertEquals(CheckoutLocalizationKey.BLIK_CODE_INVALID, actual.blikCode.errorMessage)
        }

        @Test
        fun `when blik code is too short, then validate should return state with error message`() {
            val state = createState(blikCode = "12345")

            val actual = validator.validate(state)

            assertEquals(CheckoutLocalizationKey.BLIK_CODE_INVALID, actual.blikCode.errorMessage)
        }

        @Test
        fun `when blik code is too long, then validate should return state with error message`() {
            val state = createState(blikCode = "1234567")

            val actual = validator.validate(state)

            assertEquals(CheckoutLocalizationKey.BLIK_CODE_INVALID, actual.blikCode.errorMessage)
        }

        @Test
        fun `when blik code contains non-numeric characters, then validate should return state with error message`() {
            val state = createState(blikCode = "12345a")

            val actual = validator.validate(state)

            assertEquals(CheckoutLocalizationKey.BLIK_CODE_INVALID, actual.blikCode.errorMessage)
        }
    }

    @Nested
    inner class IsValidTest {

        @Test
        fun `when blik code has no error, then isValid should return true`() {
            val state = createState(blikCode = VALID_BLIK_CODE, errorMessage = null)

            val actual = validator.isValid(state)

            assertTrue(actual)
        }

        @Test
        fun `when blik code has an error, then isValid should return false`() {
            val state = createState(
                blikCode = "123",
                errorMessage = CheckoutLocalizationKey.BLIK_CODE_INVALID,
            )

            val actual = validator.isValid(state)

            assertFalse(actual)
        }
    }

    private fun createState(
        blikCode: String,
        errorMessage: CheckoutLocalizationKey? = null,
    ) = BlikComponentState(
        blikCode = TextInputComponentState(
            text = blikCode,
            errorMessage = errorMessage,
            isFocused = false,
            showError = false,
        ),
        isLoading = false,
    )

    companion object {
        private const val VALID_BLIK_CODE = "123456"
    }
}
