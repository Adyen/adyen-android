/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/10/2021.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.util.CardNumberValidation
import com.adyen.checkout.card.util.CardValidationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.GregorianCalendar

internal class CardValidationUtilsTest {

    @Test
    fun validateCardNumber_PlainValidNumber_ExpectValid() {
        val number = "5454545454545454"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            isBrandSupported = true
        )
        assertEquals(CardNumberValidation.VALID, validation)
    }

    @Test
    fun validateCardNumber_ValidWithSpaces_ExpectValid() {
        val number = "3700 0000 0000 002"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            isBrandSupported = true
        )
        assertEquals(CardNumberValidation.VALID, validation)
    }

    @Test
    fun validateCardNumber_ValidWithRandomSpaces_ExpectValid() {
        val number = "55 770 0005 57 7  00 04"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            isBrandSupported = true
        )
        assertEquals(CardNumberValidation.VALID, validation)
    }

    @Test
    fun validateCardNumber_NumberWithCharacters_ExpectInvalid() {
        val number = "2137f7834a2390"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            // set to false to make sure INVALID_ILLEGAL_CHARACTERS is checked before INVALID_UNSUPPORTED_BRAND
            isBrandSupported = false
        )
        assertEquals(CardNumberValidation.INVALID_ILLEGAL_CHARACTERS, validation)
    }

    @Test
    fun validateCardNumber_NumberWithSymbols_ExpectInvalid() {
        val number = "287,7482-3674"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            // set to false to make sure INVALID_ILLEGAL_CHARACTERS is checked before INVALID_UNSUPPORTED_BRAND
            isBrandSupported = false
        )
        assertEquals(CardNumberValidation.INVALID_ILLEGAL_CHARACTERS, validation)
    }

    @Test
    fun validateCardNumber_ShortNumber_ExpectTooShort() {
        val number = "1234123"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            // set to false to make sure INVALID_TOO_SHORT is checked before INVALID_UNSUPPORTED_BRAND
            isBrandSupported = false
        )
        assertEquals(CardNumberValidation.INVALID_TOO_SHORT, validation)
    }

    @Test
    fun validateCardNumber_LongNumber_ExpectTooLong() {
        val number = "37467643756457884754"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            // set to false to make sure INVALID_TOO_LONG is checked before INVALID_UNSUPPORTED_BRAND
            isBrandSupported = false
        )
        assertEquals(CardNumberValidation.INVALID_TOO_LONG, validation)
    }

    @Test
    fun validateCardNumber_ValidNumberUnsupportedBrand_ExpectUnsupportedBrand() {
        val number = "6771 7980 2100 0008"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            isBrandSupported = false
        )
        assertEquals(CardNumberValidation.INVALID_UNSUPPORTED_BRAND, validation)
    }

    @Test
    fun validateCardNumber_FailsLuhnCheck_ExpectInvalidLuhnCheck() {
        val number = "8475 1789 7235 6236"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = true,
            isBrandSupported = true
        )
        assertEquals(CardNumberValidation.INVALID_LUHN_CHECK, validation)
    }

    @Test
    fun validateCardNumber_FailsLuhnCheckWithFalseEnableLuhnCheck_ExpectValid() {
        val number = "192382023091310912"
        val validation = CardValidationUtils.validateCardNumber(
            number = number,
            enableLuhnCheck = false,
            isBrandSupported = true
        )
        assertEquals(CardNumberValidation.VALID, validation)
    }

    @Test
    fun isInMaxYearRange_ExpiryDateThirtyYearsInFuture_ExpectTrue() {
        val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
        val expiryDate = ExpiryDate(12, 2052) // 12/2052 (last valid date in future)
        val actual = CardValidationUtils.isInMaxYearRange(expiryDate, mockCalendarInstance)
        assertEquals(true, actual)
    }

    @Test
    fun isInMaxYearRange_ExpiryDateMoreThanThirtyYearsInFuture_ExpectFalse() {
        val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
        val expiryDate = ExpiryDate(1, 2053) // 01/2053 (first invalid date in future)
        val actual = CardValidationUtils.isInMaxYearRange(expiryDate, mockCalendarInstance)
        assertEquals(false, actual)
    }

    @Test
    fun isInMaxYearRange_ExpiryDateEightYearsInFuture_ExpectTrue() {
        val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
        val expiryDate = ExpiryDate(1, 2030) // 01/2030
        val actual = CardValidationUtils.isInMaxYearRange(expiryDate, mockCalendarInstance)
        assertEquals(true, actual)
    }


    @Test
    fun isInMinMonthRange_ExpiryDateThreeMonthsInPast_ExpectTrue() {
        // month is 0 based in calendar
        val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
        // month is 1 based in expiry date
        val expiryDate = ExpiryDate(2, 2022) // 02/2022 (last valid date in past)
        val actual = CardValidationUtils.isInMinMonthRange(expiryDate, mockCalendarInstance)
        assertEquals(true, actual)
    }

    @Test
    fun isInMinMonthRange_ExpiryDateMoreThanThreeMonthsInPast_ExpectFalse() {
        // month is 0 based in calendar
        val mockCalendarInstance = GregorianCalendar(2022, 4, 23) // 23/05/2022
        // month is 1 based in expiry date
        val expiryDate = ExpiryDate(1, 2022) // 01/2022 (first invalid date in past)
        val actual = CardValidationUtils.isInMinMonthRange(expiryDate, mockCalendarInstance)
        assertEquals(false, actual)
    }
}
