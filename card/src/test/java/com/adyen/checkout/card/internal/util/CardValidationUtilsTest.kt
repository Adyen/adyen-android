/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/10/2021.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.internal.ui.model.INVALID_DATE
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.ui.validation.CardNumberValidationResult
import com.adyen.checkout.core.ui.validation.CardSecurityCodeValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardValidationUtilsTest {

    @Nested
    @DisplayName("when validating card number and")
    inner class ValidateCardNumberTest {

        @Test
        fun `number is valid with brand supported, then result should be valid`() {
            val validation = CardValidationUtils.validateCardNumber(
                validationResult = CardNumberValidationResult.Valid(),
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number is valid with brand unsupported, then result should be invalid unsupported brand`() {
            val validation = CardValidationUtils.validateCardNumber(
                validationResult = CardNumberValidationResult.Valid(),
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_UNSUPPORTED_BRAND, validation)
        }

        @Test
        fun `number is invalid with illegal characters, then result should be invalid illegal characters`() {
            val validation = CardValidationUtils.validateCardNumber(
                validationResult = CardNumberValidationResult.Invalid.IllegalCharacters(),
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.INVALID_ILLEGAL_CHARACTERS, validation)
        }

        @Test
        fun `number is too long, then result should be invalid too long`() {
            val validation = CardValidationUtils.validateCardNumber(
                validationResult = CardNumberValidationResult.Invalid.TooLong(),
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.INVALID_TOO_LONG, validation)
        }

        @Test
        fun `number is too short, then result should be invalid too short`() {
            val validation = CardValidationUtils.validateCardNumber(
                validationResult = CardNumberValidationResult.Invalid.TooShort(),
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.INVALID_TOO_SHORT, validation)
        }

        @Test
        fun `number is invalid due to failing luhn check, then result should be invalid luhn check`() {
            val validation = CardValidationUtils.validateCardNumber(
                validationResult = CardNumberValidationResult.Invalid.LuhnCheck(),
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.INVALID_LUHN_CHECK, validation)
        }
    }

    @Nested
    inner class ValidateExpiryDateTest {

        @Test
        fun `date is valid with field policy optional, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                ExpiryDate(12, 2030),
                CardExpiryDateValidationResult.Valid(),
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.VALID, actual)
        }

        @Test
        fun `date is valid with field policy hidden, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                ExpiryDate(12, 2030),
                CardExpiryDateValidationResult.Valid(),
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.VALID, actual)
        }

        @Test
        fun `date is too far in the future with field policy optional, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                ExpiryDate(12, 2099),
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE, actual)
        }

        @Test
        fun `date is too far in the future with field policy hidden, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                ExpiryDate(12, 2099),
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE, actual)
        }

        @Test
        fun `date is too old with field policy optional, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                ExpiryDate(12, 2012),
                CardExpiryDateValidationResult.Invalid.TooOld(),
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_OLD, actual)
        }

        @Test
        fun `date is too old with field policy hidden, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                ExpiryDate(12, 2012),
                CardExpiryDateValidationResult.Invalid.TooOld(),
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_OLD, actual)
        }

        @Test
        fun `date is empty with field policy required, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                EMPTY_DATE,
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
                Brand.FieldPolicy.REQUIRED,
            )

            assertEquals(CardExpiryDateValidation.INVALID_OTHER_REASON, actual)
        }

        @Test
        fun `date is empty with field policy optional, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                EMPTY_DATE,
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.VALID_NOT_REQUIRED, actual)
        }

        @Test
        fun `date is empty with field policy hidden, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                EMPTY_DATE,
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.VALID_NOT_REQUIRED, actual)
        }

        @Test
        fun `date is invalid with field policy required, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                INVALID_DATE,
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
                Brand.FieldPolicy.REQUIRED,
            )

            assertEquals(CardExpiryDateValidation.INVALID_OTHER_REASON, actual)
        }

        @Test
        fun `date is invalid with field policy optional, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                INVALID_DATE,
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.INVALID_OTHER_REASON, actual)
        }

        @Test
        fun `date is invalid with field policy hidden, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                INVALID_DATE,
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.INVALID_OTHER_REASON, actual)
        }
    }

    @Nested
    inner class ValidateSecurityCodeTest {
        @Test
        fun `cvc is valid with field policy required, then result should be valid`() {
            val cvc = "546"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.REQUIRED,
                CardSecurityCodeValidationResult.Valid(),
            )
            assertEquals(CardSecurityCodeValidation.VALID, actual)
        }

        @Test
        fun `cvc is valid with field policy optional, then result should be valid`() {
            val cvc = "345"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.OPTIONAL,
                CardSecurityCodeValidationResult.Valid(),
            )
            assertEquals(CardSecurityCodeValidation.VALID, actual)
        }

        @Test
        fun `cvc is valid with field policy hidden, then result should be valid`() {
            val cvc = "156"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.HIDDEN,
                CardSecurityCodeValidationResult.Valid(),
            )
            assertEquals(CardSecurityCodeValidation.VALID_HIDDEN, actual)
        }

        @Test
        fun `cvc is invalid with field policy required, then result should be invalid`() {
            val cvc = "77"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.REQUIRED,
                CardSecurityCodeValidationResult.Invalid(),
            )
            assertEquals(CardSecurityCodeValidation.INVALID, actual)
        }

        @Test
        fun `cvc is invalid with field policy optional, then result should be invalid`() {
            val cvc = "9"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.OPTIONAL,
                CardSecurityCodeValidationResult.Invalid(),
            )
            assertEquals(CardSecurityCodeValidation.INVALID, actual)
        }

        @Test
        fun `cvc is invalid with field policy hidden, then result should be valid`() {
            val cvc = "1358"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.HIDDEN,
                CardSecurityCodeValidationResult.Invalid(),
            )
            assertEquals(CardSecurityCodeValidation.VALID_HIDDEN, actual)
        }

        @Test
        fun `cvc is empty with field policy required, then result should be invalid`() {
            val cvc = ""
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.REQUIRED,
                CardSecurityCodeValidationResult.Invalid(),
            )
            assertEquals(CardSecurityCodeValidation.INVALID, actual)
        }

        @Test
        fun `cvc is empty with field policy optional, then result should be valid`() {
            val cvc = ""
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.OPTIONAL,
                CardSecurityCodeValidationResult.Invalid(),
            )
            assertEquals(CardSecurityCodeValidation.VALID_OPTIONAL_EMPTY, actual)
        }

        @Test
        fun `cvc is empty with field policy hidden, then result should be valid`() {
            val cvc = ""
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                InputFieldUIState.HIDDEN,
                CardSecurityCodeValidationResult.Invalid(),
            )
            assertEquals(CardSecurityCodeValidation.VALID_HIDDEN, actual)
        }
    }
}
