/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/8/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Calendar
import java.util.GregorianCalendar

class ExpiryDateValidationUtilsTest {

    @ParameterizedTest
    @MethodSource("expiryDateValidSource")
    fun `when validating valid expiry date, then valid result is returned`(
        expiryDateInput: ExpiryDate,
        calendar: Calendar,
    ) {
        val actual = ExpiryDateValidationUtils.validateExpiryDate(expiryDateInput, calendar)

        assertTrue(actual is ExpiryDateValidationResult.Valid)
    }

    @Test
    fun `when validating expiry date too much in the future, then invalid result is returned`() {
        // Date more than 30 years in future
        val expiryDate = ExpiryDate(1, 2053) // 01/2053 (first invalid date in future)
        val calendar = GregorianCalendar(2022, 4, 23) // 23/05/2022

        val actual = ExpiryDateValidationUtils.validateExpiryDate(expiryDate, calendar)

        assertTrue(actual is ExpiryDateValidationResult.InvalidTooFarInTheFuture)
    }

    @Test
    fun `when validating expiry date too old, then invalid result is returned`() {
        // Date more than 3 months in past
        val expiryDate = ExpiryDate(1, 2022) // 01/2022 (first invalid date in past)
        val calendar = GregorianCalendar(2022, 4, 23) // 23/05/2022

        val actual = ExpiryDateValidationUtils.validateExpiryDate(expiryDate, calendar)

        assertTrue(actual is ExpiryDateValidationResult.InvalidTooOld)
    }

    @Test
    fun `when validating empty expiry date, then invalid result is returned`() {
        val expiryDate = ExpiryDate.EMPTY_DATE
        val calendar = GregorianCalendar.getInstance()

        val actual = ExpiryDateValidationUtils.validateExpiryDate(expiryDate, calendar)

        assertTrue(actual is ExpiryDateValidationResult.InvalidExpiryDate)
        assertFalse((actual as ExpiryDateValidationResult.InvalidExpiryDate).isDateFormatInvalid)
    }

    @Test
    fun `when validating invalid expiry date, then invalid result is returned`() {
        val expiryDate = ExpiryDate.INVALID_DATE
        val calendar = GregorianCalendar.getInstance()

        val actual = ExpiryDateValidationUtils.validateExpiryDate(expiryDate, calendar)

        assertTrue(actual is ExpiryDateValidationResult.InvalidExpiryDate)
        assertTrue((actual as ExpiryDateValidationResult.InvalidExpiryDate).isDateFormatInvalid)
    }

    companion object {

        @JvmStatic
        fun expiryDateValidSource() = listOf(
            // Date 30 years in future
            arguments(
                ExpiryDate(12, 2052), // 12/2052 (last valid date in future)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
            ),
            // Date 8 years in future
            arguments(
                ExpiryDate(1, 2030), // 01/2030
                GregorianCalendar(2022, 4, 23), // 23/05/2022
            ),
            // Date 1 month in past
            arguments(
                ExpiryDate(4, 2022), // 04/2022 (last valid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
            ),
            // Date 3 months in past
            arguments(
                ExpiryDate(2, 2022), // 02/2022 (last valid date in past)
                GregorianCalendar(2022, 4, 23), // 23/05/2022
            ),
            // Date 1 year in future
            arguments(
                ExpiryDate(1, 2023), // 01/2023
                GregorianCalendar(2022, 4, 23), // 23/05/2022
            ),
        )
    }
}
