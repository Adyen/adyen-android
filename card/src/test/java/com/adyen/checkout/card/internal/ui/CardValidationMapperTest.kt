/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.util.CardExpiryDateValidation
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardSecurityCodeValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardValidationMapperTest {

    private val cardValidationMapper = CardValidationMapper()

    @Nested
    @DisplayName("when validating expiry date and")
    inner class ValidateExpiryDateTest {

        @Test
        fun `date is valid, then correct fieldState should be returned`() {
            val expiryDate = ExpiryDate(4, 2025) // 04/2025
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                CardExpiryDateValidation.VALID,
            )

            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is valid, then result should be valid`() {
            val expiryDate = ExpiryDate(4, 2040) // 04/2040
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                CardExpiryDateValidation.VALID,
            )

            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is too far in the future, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2099) // 04/2099
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_far_in_future

            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is too old, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2020) // 04/2020
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                CardExpiryDateValidation.INVALID_TOO_OLD,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old

            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is not in correct format, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 20) // 04/2020
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                CardExpiryDateValidation.INVALID_DATE_FORMAT,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid

            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is invalid, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2020) // 04/2020
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                CardExpiryDateValidation.INVALID_OTHER_REASON,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid

            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }
    }

    @Nested
    @DisplayName("when validating cvc and")
    inner class ValidateSecurityCodeTest {

        @Test
        fun `cvc is valid with field policy required, then result should be valid`() {
            val cvc = "546"
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.REQUIRED,
                CardSecurityCodeValidationResult.VALID,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is valid with field policy optional, then result should be valid`() {
            val cvc = "345"
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.OPTIONAL,
                CardSecurityCodeValidationResult.VALID,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is valid with field policy hidden, then result should be valid`() {
            val cvc = "156"
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.HIDDEN,
                CardSecurityCodeValidationResult.VALID,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is invalid with field policy required, then result should be invalid`() {
            val cvc = "77"
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.REQUIRED,
                CardSecurityCodeValidationResult.INVALID,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is invalid with field policy optional, then result should be invalid`() {
            val cvc = "9"
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.OPTIONAL,
                CardSecurityCodeValidationResult.INVALID,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is invalid with field policy hidden, then result should be valid`() {
            val cvc = "1358"
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.HIDDEN,
                CardSecurityCodeValidationResult.INVALID,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is empty with field policy required, then result should be invalid`() {
            val cvc = ""
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.REQUIRED,
                CardSecurityCodeValidationResult.INVALID,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is empty with field policy optional, then result should be valid`() {
            val cvc = ""
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.OPTIONAL,
                CardSecurityCodeValidationResult.INVALID,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is empty with field policy hidden, then result should be valid`() {
            val cvc = ""
            val actual = cardValidationMapper.mapSecurityCodeValidation(
                cvc,
                InputFieldUIState.HIDDEN,
                CardSecurityCodeValidationResult.INVALID,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }
    }
}
