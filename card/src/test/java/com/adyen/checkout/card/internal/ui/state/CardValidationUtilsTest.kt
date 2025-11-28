/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/11/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardValidationUtilsTest {

    @Nested
    @DisplayName("when validating holder name and")
    inner class ValidateHolderNameTest {

        @Test
        fun `holder name is required and blank, then result should be invalid`() {
            val validation = CardValidationUtils.validateHolderName(
                holderName = "",
                isRequired = true,
            )
            assertEquals(CardHolderNameValidation.INVALID_BLANK, validation)
        }

        @Test
        fun `holder name is required and whitespace only, then result should be invalid`() {
            val validation = CardValidationUtils.validateHolderName(
                holderName = "   ",
                isRequired = true,
            )
            assertEquals(CardHolderNameValidation.INVALID_BLANK, validation)
        }

        @Test
        fun `holder name is required and not blank, then result should be valid`() {
            val validation = CardValidationUtils.validateHolderName(
                holderName = "J. Smith",
                isRequired = true,
            )
            assertEquals(CardHolderNameValidation.VALID, validation)
        }

        @Test
        fun `holder name is not required and blank, then result should be valid`() {
            val validation = CardValidationUtils.validateHolderName(
                holderName = "",
                isRequired = false,
            )
            assertEquals(CardHolderNameValidation.VALID, validation)
        }

        @Test
        fun `holder name is not required and not blank, then result should be valid`() {
            val validation = CardValidationUtils.validateHolderName(
                holderName = "J. Smith",
                isRequired = false,
            )
            assertEquals(CardHolderNameValidation.VALID, validation)
        }
    }
}
