/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/11/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

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

    @ParameterizedTest
    @MethodSource("kcpBirthDateOrTaxNumberSource")
    fun `when validating KCP birth date or tax number then result should match expected value`(
        kcpBirthDateOrTaxNumber: String,
        requirementPolicy: RequirementPolicy?,
        expectedValidation: KCPBirthDateOrTaxNumberValidation,
    ) {
        val validation = CardValidationUtils.validateKCPBirthDateOrTaxNumber(
            kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumber,
            requirementPolicy = requirementPolicy,
        )
        assertEquals(expectedValidation, validation)
    }

    @ParameterizedTest
    @MethodSource("kcpCardPasswordSource")
    fun `when validating KCP card password then result should match expected value`(
        kcpCardPassword: String,
        requirementPolicy: RequirementPolicy?,
        expectedValidation: KCPCardPasswordValidation,
    ) {
        val validation = CardValidationUtils.validateKCPCardPassword(
            kcpCardPassword = kcpCardPassword,
            requirementPolicy = requirementPolicy,
        )
        assertEquals(expectedValidation, validation)
    }

    companion object {

        @JvmStatic
        fun kcpBirthDateOrTaxNumberSource() = listOf(
            // kcpBirthDateOrTaxNumber, requirementPolicy, expectedValidation
            arguments("", RequirementPolicy.Required, KCPBirthDateOrTaxNumberValidation.INVALID),
            // invalid length
            arguments("12", RequirementPolicy.Required, KCPBirthDateOrTaxNumberValidation.INVALID),
            arguments("12345678", RequirementPolicy.Required, KCPBirthDateOrTaxNumberValidation.INVALID),
            // invalid format
            arguments("123456", RequirementPolicy.Required, KCPBirthDateOrTaxNumberValidation.INVALID),
            // valid 6 digit yyMMdd format
            arguments("251210", RequirementPolicy.Required, KCPBirthDateOrTaxNumberValidation.VALID),
            // valid 10 digits
            arguments("1234567890", RequirementPolicy.Required, KCPBirthDateOrTaxNumberValidation.VALID),
            // hidden
            arguments("", RequirementPolicy.Hidden, KCPBirthDateOrTaxNumberValidation.VALID),
            // optional
            arguments("", RequirementPolicy.Optional, KCPBirthDateOrTaxNumberValidation.VALID),
            arguments("251210", RequirementPolicy.Optional, KCPBirthDateOrTaxNumberValidation.VALID),
            arguments("12", RequirementPolicy.Optional, KCPBirthDateOrTaxNumberValidation.INVALID),
        )

        @JvmStatic
        fun kcpCardPasswordSource() = listOf(
            // kcpCardPassword, requirementPolicy, expectedValidation
            arguments("", RequirementPolicy.Required, KCPCardPasswordValidation.INVALID),
            // invalid length
            arguments("1", RequirementPolicy.Required, KCPCardPasswordValidation.INVALID),
            arguments("123", RequirementPolicy.Required, KCPCardPasswordValidation.INVALID),
            // valid 2 digits
            arguments("12", RequirementPolicy.Required, KCPCardPasswordValidation.VALID),
            // hidden
            arguments("", RequirementPolicy.Hidden, KCPCardPasswordValidation.VALID),
            // optional
            arguments("", RequirementPolicy.Optional, KCPCardPasswordValidation.VALID),
            arguments("25", RequirementPolicy.Optional, KCPCardPasswordValidation.VALID),
            arguments("1", RequirementPolicy.Optional, KCPCardPasswordValidation.INVALID),
        )
    }
}
