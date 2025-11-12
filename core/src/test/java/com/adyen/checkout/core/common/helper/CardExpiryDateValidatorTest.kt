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
        expiryDateInput: String,
        calendar: Calendar,
        expectedValidationResult: CardExpiryDateValidationResult,
    ) {
        val actualResult = CardExpiryDateValidator.validateExpiryDate(expiryDateInput, calendar)

        assertEquals(expectedValidationResult.javaClass, actualResult.javaClass)
    }

    companion object {

        @JvmStatic
        fun expiryDateValidationSource() = listOf(
            // Invalid expiry date
            arguments(
                "0/0",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            arguments(
                "-1/-1",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // Non existing month
            arguments(
                "15/30",
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.NonParseableDate(),
            ),
            // Date 30 years in future
            arguments(
                "12/52", // last valid date in future
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date more than 30 years in future
            arguments(
                "01/53", // first invalid date in future
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
            ),
            // Date 8 years in future
            arguments(
                "01/30",
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date 1 month in past
            arguments(
                "04/22", // last valid date in past
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date 3 months in past
            arguments(
                "02/22", // last valid date in past
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date more than 3 months in past
            arguments(
                "01/22", // first invalid date in past
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Invalid.TooOld(),
            ),
            // Date 1 year in future
            arguments(
                "01/23",
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date crossing centuries, valid
            arguments(
                "01/05",
                GregorianCalendar(1975, 1, 1), // 01/01/1975
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date crossing centuries, one year too far in the future
            arguments(
                "01/06",
                GregorianCalendar(1975, 1, 1), // 01/01/1975
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
            ),
        )
    }
}
