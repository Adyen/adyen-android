/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/10/2024.
 */

package com.adyen.checkout.core.ui.validation

import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.internal.ui.model.INVALID_DATE
import com.adyen.checkout.core.ui.model.ExpiryDate
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
        expiryDateInput: ExpiryDate,
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
                EMPTY_DATE,
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.OtherReason(),
            ),
            arguments(
                INVALID_DATE,
                GregorianCalendar.getInstance(),
                CardExpiryDateValidationResult.Invalid.DateFormat(),
            ),
            // Date 30 years in future
            arguments(
                ExpiryDate(12, 2052), // 12/2052 (last valid date in future)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date more than 30 years in future
            arguments(
                ExpiryDate(1, 2053), // 01/2053 (first invalid date in future)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Invalid.TooFarInTheFuture(),
            ),
            // Date 8 years in future
            arguments(
                ExpiryDate(1, 2030), // 01/2030
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date 1 month in past
            arguments(
                ExpiryDate(4, 2022), // 04/2022 (last valid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date 3 months in past
            arguments(
                ExpiryDate(2, 2022), // 02/2022 (last valid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
            // Date more than 3 months in past
            arguments(
                ExpiryDate(1, 2022), // 01/2022 (first invalid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Invalid.TooOld(),
            ),
            // Date 1 year in future
            arguments(
                ExpiryDate(1, 2023), // 01/2023
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                CardExpiryDateValidationResult.Valid(),
            ),
        )
    }
}
