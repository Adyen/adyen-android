/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/10/2021.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.ui.validation.CardSecurityCodeValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CardValidationUtilsTest {

    private val cardValidationMapper = CardValidationMapper()

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
    @DisplayName("when validating expiry date and")
    inner class ValidateExpiryDateTest {

        // TODO extract these tests to a CardValidationMapper class
        @Test
        fun `date is valid, then correct fieldState should be returned`() {
            val expiryDate = ExpiryDate(4, 2025) // 04/2025
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.REQUIRED,
                CardExpiryDateValidationResult.VALID,
            )

            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is valid, then result should be valid`() {
            val expiryDate = ExpiryDate(4, 2040) // 04/2040
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.REQUIRED,
                CardExpiryDateValidationResult.VALID,
            )

            assertEquals(Validation.Valid, actual.validation)
        }

        @Test
        fun `date is too far in the future, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2099) // 04/2099
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.REQUIRED,
                CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_far_in_future

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is too old, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2020) // 04/2020
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.REQUIRED,
                CardExpiryDateValidationResult.INVALID_TOO_OLD,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is valid with field policy optional, then result should be valid`() {
            val expiryDate = ExpiryDate(4, 2040) // 04/2040
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.OPTIONAL,
                CardExpiryDateValidationResult.VALID,
            )

            assertEquals(Validation.Valid, actual.validation)
        }

        @Test
        fun `date is valid with field policy hidden, then result should be valid`() {
            val expiryDate = ExpiryDate(4, 2040) // 04/2040
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.HIDDEN,
                CardExpiryDateValidationResult.VALID,
            )

            assertEquals(Validation.Valid, actual.validation)
        }

        @Test
        fun `date is too far in the future with field policy optional, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2099) // 04/2099
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.OPTIONAL,
                CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_far_in_future

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is too far in the future with field policy hidden, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2099) // 04/2099
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.HIDDEN,
                CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_far_in_future

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is too old with field policy optional, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2020) // 04/2020
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.OPTIONAL,
                CardExpiryDateValidationResult.INVALID_TOO_OLD,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is too old with field policy hidden, then result should be invalid`() {
            val expiryDate = ExpiryDate(4, 2020) // 04/2020
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.HIDDEN,
                CardExpiryDateValidationResult.INVALID_TOO_OLD,
            )

            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is empty with field policy required, then result should be invalid`() {
            val expiryDate = ExpiryDate.EMPTY_DATE
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.REQUIRED,
                CardExpiryDateValidationResult.INVALID_OTHER_REASON,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is empty with field policy optional, then result should be valid`() {
            val expiryDate = ExpiryDate.EMPTY_DATE
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.OPTIONAL,
                CardExpiryDateValidationResult.INVALID_OTHER_REASON,
            )
            assertEquals(Validation.Valid, actual.validation)
        }

        @Test
        fun `date is empty with field policy hidden, then result should be valid`() {
            val expiryDate = ExpiryDate.EMPTY_DATE
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.HIDDEN,
                CardExpiryDateValidationResult.INVALID_OTHER_REASON,
            )

            assertEquals(Validation.Valid, actual.validation)
        }

        @Test
        fun `date is invalid with field policy required, then result should be invalid`() {
            val expiryDate = ExpiryDate.INVALID_DATE
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.REQUIRED,
                CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is invalid with field policy optional, then result should be invalid`() {
            val expiryDate = ExpiryDate.INVALID_DATE
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.OPTIONAL,
                CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
        }

        @Test
        fun `date is invalid with field policy hidden, then result should be invalid`() {
            val expiryDate = ExpiryDate.INVALID_DATE
            val actual = cardValidationMapper.mapExpiryDateValidation(
                expiryDate,
                Brand.FieldPolicy.HIDDEN,
                CardExpiryDateValidationResult.INVALID_DATE_FORMAT,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid

            assertEquals(Validation.Invalid(expectedInvalidReason), actual.validation)
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
