/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/10/2021.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.GregorianCalendar

internal class CardValidationUtilsTest {

    @Nested
    @DisplayName("when validating card number and")
    inner class ValidateCardNumberTest {

        @Test
        fun `number is valid without separators then result should be valid`() {
            val number = "5454545454545454"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number is valid with formatting spacing then result should be valid`() {
            val number = "3700 0000 0000 002"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number is valid with random spaces then result should be valid`() {
            val number = "55 770 0005 57 7  00 04"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.VALID, validation)
        }

        @Test
        fun `number contains alphabetical characters then result should be invalid`() {
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
        fun `number contains illegal characters then result should be invalid`() {
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
        fun `number is too short then result should be invalid`() {
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
        fun `number is too long then result should be invalid`() {
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
        fun `brand is unsupported then result should be invalid`() {
            val number = "6771 7980 2100 0008"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = false,
            )
            assertEquals(CardNumberValidation.INVALID_UNSUPPORTED_BRAND, validation)
        }

        @Test
        fun `luhn check fails then result should be invalid`() {
            val number = "8475 1789 7235 6236"
            val validation = CardValidationUtils.validateCardNumber(
                number = number,
                enableLuhnCheck = true,
                isBrandSupported = true,
            )
            assertEquals(CardNumberValidation.INVALID_LUHN_CHECK, validation)
        }

        @Test
        fun `luhn check fails but luhn check is disabled then result should be valid`() {
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

        @Test
        fun `date is 30 years in the future then result should be valid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(12, 2052) // 12/2052 (last valid date in future)
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is more than 30 years in the future then result should be invalid as not within max range`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2053) // 01/2053 (first invalid date in future)
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_far_in_future
            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is 8 years in the future then result should be valid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2030) // 01/2030
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is 1 month in the past then result should be valid`() {
            // month is 0 based in calendar
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            // month is 1 based in expiry date
            val expiryDate = ExpiryDate(4, 2022) // 04/2022 (last valid date in past)
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is 3 months in the past then result should be valid`() {
            // month is 0 based in calendar
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            // month is 1 based in expiry date
            val expiryDate = ExpiryDate(2, 2022) // 02/2022 (last valid date in past)
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is more than 3 months in the past then result should be invalid as not within min range`() {
            // month is 0 based in calendar
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            // month is 1 based in expiry date
            val expiryDate = ExpiryDate(1, 2022) // 01/2022 (first invalid date in past)
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old
            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is 1 year in the future then result should be valid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2023) // 01/2023
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is valid with field policy required then result should be valid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2023) // 01/2023
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is valid with field policy optional then result should be valid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2023) // 01/2023
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.OPTIONAL,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is valid with field policy hidden then result should be valid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2023) // 01/2023
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.HIDDEN,
                calendar = mockCalendarInstance,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is invalid with field policy required then result should be invalid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2022) // 01/2022
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
                calendar = mockCalendarInstance,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old
            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is invalid with field policy optional then result should be invalid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2022) // 01/2022
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.OPTIONAL,
                calendar = mockCalendarInstance,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old
            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is invalid with field policy hidden then result should be invalid`() {
            val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
            val expiryDate = ExpiryDate(1, 2022) // 01/2022
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.HIDDEN,
                calendar = mockCalendarInstance,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid_too_old
            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is empty with field policy required then result should be invalid`() {
            val expiryDate = ExpiryDate.EMPTY_DATE
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.REQUIRED,
            )
            val expectedInvalidReason = R.string.checkout_expiry_date_not_valid
            assertEquals(FieldState(expiryDate, Validation.Invalid(expectedInvalidReason)), actual)
        }

        @Test
        fun `date is empty with field policy optional then result should be valid`() {
            val expiryDate = ExpiryDate.EMPTY_DATE
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.OPTIONAL,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }

        @Test
        fun `date is empty with field policy hidden then result should be valid`() {
            val expiryDate = ExpiryDate.EMPTY_DATE
            val actual = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = Brand.FieldPolicy.HIDDEN,
            )
            assertEquals(FieldState(expiryDate, Validation.Valid), actual)
        }
    }

    @Nested
    @DisplayName("when validating cvc and")
    inner class ValidateSecurityCodeTest {

        @Test
        fun `cvc is empty then result should be invalid`() {
            val cvc = ""
            val actual =
                CardValidationUtils.validateSecurityCode(cvc, getDetectedCardType(), InputFieldUIState.REQUIRED)
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is 1 digit then result should be invalid`() {
            val cvc = "7"
            val actual =
                CardValidationUtils.validateSecurityCode(cvc, getDetectedCardType(), InputFieldUIState.REQUIRED)
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is 2 digits then result should be invalid`() {
            val cvc = "12"
            val actual =
                CardValidationUtils.validateSecurityCode(cvc, getDetectedCardType(), InputFieldUIState.REQUIRED)
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is 3 digits then result should be valid`() {
            val cvc = "737"
            val actual =
                CardValidationUtils.validateSecurityCode(cvc, getDetectedCardType(), InputFieldUIState.REQUIRED)
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is 4 digits then result should be invalid`() {
            val cvc = "8689"
            val actual =
                CardValidationUtils.validateSecurityCode(cvc, getDetectedCardType(), InputFieldUIState.REQUIRED)
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is 6 digits then result should be invalid`() {
            val cvc = "457835"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(),
                InputFieldUIState.REQUIRED,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is 3 digits with AMEX then result should be invalid`() {
            val cvc = "737"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cardBrand = CardBrand(CardType.AMERICAN_EXPRESS)),
                cvcUIState = InputFieldUIState.REQUIRED,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is 4 digits with AMEX then result should be valid`() {
            val cvc = "8689"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cardBrand = CardBrand(CardType.AMERICAN_EXPRESS)),
                cvcUIState = InputFieldUIState.REQUIRED,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc has invalid characters then result should be invalid`() {
            val cvc = "1%y"
            val actual =
                CardValidationUtils.validateSecurityCode(cvc, getDetectedCardType(), InputFieldUIState.REQUIRED)
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is valid with field policy required then result should be valid`() {
            val cvc = "546"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED),
                cvcUIState = InputFieldUIState.REQUIRED,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is valid with field policy optional then result should be valid`() {
            val cvc = "345"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
                cvcUIState = InputFieldUIState.OPTIONAL,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is valid with field policy hidden then result should be valid`() {
            val cvc = "156"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.HIDDEN),
                cvcUIState = InputFieldUIState.HIDDEN,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is invalid with field policy required then result should be invalid`() {
            val cvc = "77"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED),
                InputFieldUIState.REQUIRED,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is invalid with field policy optional then result should be invalid`() {
            val cvc = "9"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
                InputFieldUIState.OPTIONAL,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is invalid with field policy hidden then result should be valid`() {
            val cvc = "1358"
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.HIDDEN),
                cvcUIState = InputFieldUIState.HIDDEN,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is empty with field policy required then result should be invalid`() {
            val cvc = ""
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.REQUIRED),
                cvcUIState = InputFieldUIState.REQUIRED,
            )
            assertEquals(FieldState(cvc, Validation.Invalid(R.string.checkout_security_code_not_valid)), actual)
        }

        @Test
        fun `cvc is empty with field policy optional then result should be valid`() {
            val cvc = ""
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.OPTIONAL),
                cvcUIState = InputFieldUIState.OPTIONAL,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        @Test
        fun `cvc is empty with field policy hidden then result should be valid`() {
            val cvc = ""
            val actual = CardValidationUtils.validateSecurityCode(
                cvc,
                getDetectedCardType(cvcPolicy = Brand.FieldPolicy.HIDDEN),
                cvcUIState = InputFieldUIState.HIDDEN,
            )
            assertEquals(FieldState(cvc, Validation.Valid), actual)
        }

        private fun getDetectedCardType(
            cardBrand: CardBrand = CardBrand(CardType.VISA),
            cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        ): DetectedCardType {
            return DetectedCardType(
                cardBrand = cardBrand,
                isReliable = false,
                enableLuhnCheck = true,
                cvcPolicy = cvcPolicy,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            )
        }
    }
}
