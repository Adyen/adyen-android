/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/10/2021.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardValidationUtilsTest {

    @Nested
    @DisplayName("when validating card number and")
    inner class ValidateCardNumberTest {

        @Test
        fun `number is valid without separators, then result should be valid`() {
            val number = "5454545454545454"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number is valid with formatting spacing, then result should be valid`() {
            val number = "3700 0000 0000 002"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number is valid with random spaces, then result should be valid`() {
            val number = "55 770 0005 57 7  00 04"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number contains alphabetical characters, then result should be invalid`() {
            val number = "2137f7834a2390"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                // set to false to make sure INVALID_ILLEGAL_CHARACTERS is checked before INVALID_UNSUPPORTED_BRAND
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_ILLEGAL_CHARACTERS, validation)
        }

        @Test
        fun `number contains illegal characters, then result should be invalid`() {
            val number = "287,7482-3674"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                // set to false to make sure INVALID_ILLEGAL_CHARACTERS is checked before INVALID_UNSUPPORTED_BRAND
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_ILLEGAL_CHARACTERS, validation)
        }

        @Test
        fun `number is too short, then result should be invalid`() {
            val number = "1234123"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                // set to false to make sure INVALID_TOO_SHORT is checked before INVALID_UNSUPPORTED_BRAND
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_TOO_SHORT, validation)
        }

        @Test
        fun `number is too long, then result should be invalid`() {
            val number = "37467643756457884754"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                // set to false to make sure INVALID_TOO_LONG is checked before INVALID_UNSUPPORTED_BRAND
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_TOO_LONG, validation)
        }

        @Test
        fun `brand is unsupported, then result should be invalid`() {
            val number = "6771 7980 2100 0008"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_UNSUPPORTED_BRAND, validation)
        }

        @Test
        fun `luhn check fails, then result should be invalid`() {
            val number = "8475 1789 7235 6236"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.INVALID_LUHN_CHECK, validation)
        }

        @Test
        fun `luhn check fails but luhn check is disabled, then result should be valid`() {
            val number = "192382023091310912"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = false,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }
    }

    @Nested
    inner class ValidateExpiryDateTest {

        @Test
        fun `date is valid with field policy optional, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.VALID,
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.VALID, actual)
        }

        @Test
        fun `date is valid with field policy hidden, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.VALID,
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.VALID, actual)
        }

        @Test
        fun `date is too far in the future with field policy optional, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE,
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE, actual)
        }

        @Test
        fun `date is too far in the future with field policy hidden, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE,
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE, actual)
        }

        @Test
        fun `date is too old with field policy optional, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_TOO_OLD,
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_OLD, actual)
        }

        @Test
        fun `date is too old with field policy hidden, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_TOO_OLD,
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.INVALID_TOO_OLD, actual)
        }

        @Test
        fun `date is empty with field policy required, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_OTHER_REASON,
                Brand.FieldPolicy.REQUIRED,
            )

            assertEquals(CardExpiryDateValidation.INVALID_OTHER_REASON, actual)
        }

        @Test
        fun `date is empty with field policy optional, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_OTHER_REASON,
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.VALID_NOT_REQUIRED, actual)
        }

        @Test
        fun `date is empty with field policy hidden, then result should be valid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_OTHER_REASON,
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.VALID_NOT_REQUIRED, actual)
        }

        @Test
        fun `date is invalid with field policy required, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
                Brand.FieldPolicy.REQUIRED,
            )

            assertEquals(CardExpiryDateValidation.INVALID_DATE_FORMAT, actual)
        }

        @Test
        fun `date is invalid with field policy optional, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
                Brand.FieldPolicy.OPTIONAL,
            )

            assertEquals(CardExpiryDateValidation.INVALID_DATE_FORMAT, actual)
        }

        @Test
        fun `date is invalid with field policy hidden, then result should be invalid`() {
            val actual = CardValidationUtils.validateExpiryDate(
                CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
                Brand.FieldPolicy.HIDDEN,
            )

            assertEquals(CardExpiryDateValidation.INVALID_DATE_FORMAT, actual)
        }
    }
}
