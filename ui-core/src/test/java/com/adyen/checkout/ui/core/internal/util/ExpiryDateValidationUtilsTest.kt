/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/8/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Calendar
import java.util.GregorianCalendar

class ExpiryDateValidationUtilsTest {

    @ParameterizedTest
    @MethodSource("expiryDateValidationSource")
    fun `when validateExpiryDate is called, then expected validation result is returned`(
        expiryDateInput: ExpiryDate,
        calendar: Calendar,
        expectedValidationResult: ExpiryDateValidationResult,
    ) {
        val actualResult = ExpiryDateValidationUtils.validateExpiryDate(expiryDateInput, calendar)

        assertEquals(expectedValidationResult, actualResult)
    }

    companion object {

        @JvmStatic
        fun expiryDateValidationSource() = listOf(
            // Invalid expiry date
            arguments(
                ExpiryDate.EMPTY_DATE,
                GregorianCalendar.getInstance(),
                ExpiryDateValidationResult.INVALID_EXPIRY_DATE,
            ),
            arguments(
                ExpiryDate.INVALID_DATE,
                GregorianCalendar.getInstance(),
                ExpiryDateValidationResult.INVALID_EXPIRY_DATE,
            ),
            // Date 30 years in future
            arguments(
                ExpiryDate(12, 2052), // 12/2052 (last valid date in future)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.VALID,
            ),
            // Date more than 30 years in future
            arguments(
                ExpiryDate(1, 2053), // 01/2053 (first invalid date in future)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE,
            ),
            // Date 8 years in future
            arguments(
                ExpiryDate(1, 2030), // 01/2030
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.VALID,
            ),
            // Date 1 month in past
            arguments(
                ExpiryDate(4, 2022), // 04/2022 (last valid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.VALID,
            ),
            // Date 3 months in past
            arguments(
                ExpiryDate(2, 2022), // 02/2022 (last valid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.VALID,
            ),
            // Date more than 3 months in past
            arguments(
                ExpiryDate(1, 2022), // 01/2022 (first invalid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.INVALID_TOO_OLD,
            ),
            // Date 1 year in future
            arguments(
                ExpiryDate(1, 2023), // 01/2023
                GregorianCalendar(2022, 4, 23), // 23/05/2022
                ExpiryDateValidationResult.VALID,
            ),
        )
    }
}
