/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/10/2024.
 */

package com.adyen.checkout.core.common.helper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Calendar
import java.util.GregorianCalendar

internal class CardExpiryDateValidatorTest {

    @ParameterizedTest
    @MethodSource("expiryDateValidationSource")
    fun `when validateExpiryDate is called, then expected validation result is returned`(
        expiryMonth: String,
        expiryYear: String,
        currentCalendar: Calendar,
        expectedValidationResult: CardExpiryDateValidationResult,
    ) {
        val actualResult = CardExpiryDateValidator.validateExpiryDate(
            expiryMonth,
            expiryYear,
            currentCalendar,
        )

        assertEquals(expectedValidationResult.javaClass, actualResult.javaClass)
    }

    companion object {

        @JvmStatic
        fun expiryDateValidationSource() = listOf(
            // Invalid characters
            arguments(
                "ab",
                "cd",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // Invalid expiry date
            arguments(
                "00",
                "00",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // non 2-digit month
            arguments(
                "1",
                "20",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // non 2-digit month
            arguments(
                "123",
                "21",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // non 2-digit year
            arguments(
                "10",
                "2",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // non 2-digit year
            arguments(
                "03",
                "203",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // Invalid numbers
            arguments(
                "-1",
                "-1",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // Non-existing month
            arguments(
                "15",
                "30",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // Valid future date
            arguments(
                "01",
                "30", // Card valid until 01/2030
                GregorianCalendar(2022, 4, 23), // current date is 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Valid past date
            arguments(
                "04",
                "22", // Card valid until 04/2022
                GregorianCalendar(2022, 4, 23), // current date is 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date 30 years in future - last valid future date
            arguments(
                "12",
                "52", // Card valid until 12/2052
                GregorianCalendar(2022, 1, 1), // current date is 1/1/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date 30 years + 1 day in future - first invalid future date
            arguments(
                "01",
                "53", // Card valid until 01/2053
                GregorianCalendar(2022, 11, 31, 23, 59, 59), // current date is 31/12/2022 23:59:59
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
            ),
            // Invalid future date
            arguments(
                "04",
                "54", // Card valid until 01/2054
                GregorianCalendar(2022, 10, 23), // current date is 23/11/2022
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
            ),
            // Date more than 4 (3+1) months in past - first invalid date
            arguments(
                "01",
                "22", // Card valid until 01/2022
                GregorianCalendar(2022, 4, 1), // current date is 01/05/2022
                CardExpiryDateValidationResult.Invalid.TooOld(),
            ),
            // Date just under 4 (3+1) months in past - last valid past date
            arguments(
                "01",
                "22", // Card valid until 02/2022
                GregorianCalendar(2022, 3, 30, 23, 59, 59), // current date is 30/04/2022 23:59:59
                CardExpiryDateValidationResult.Valid(),
            ),
            // Invalid past date
            arguments(
                "01",
                "22", // Card valid until 01/2022
                GregorianCalendar(2022, 4, 23), // current date is 23/05/2022
                CardExpiryDateValidationResult.Invalid.TooOld(),
            ),
            // Date crossing centuries, valid
            arguments(
                "05",
                "05", // Card valid until 05/2005
                GregorianCalendar(1975, 1, 1), // current date is 01/01/1975
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date crossing centuries, one year too far in the future
            arguments(
                "01",
                "06", // Card valid until 01/2006
                GregorianCalendar(1975, 1, 1), // current date is 01/01/1975
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
            ),
        )
    }
}
