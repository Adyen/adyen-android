/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/4/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.common.LoggingExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date
import java.util.GregorianCalendar

@ExtendWith(LoggingExtension::class)
class DateUtilsTest {

    @ParameterizedTest
    @MethodSource("parseToDateSource")
    fun `when parseToDate is called then date should be null if invalid or parsed if valid`(
        date: String,
        pattern: String,
        expectedResult: Date?,
    ) {
        val result = DateUtils.parseToDate(date, pattern)

        assertEquals(expectedResult, result)
    }

    companion object {

        @JvmStatic
        fun parseToDateSource() = listOf(
            // date, pattern, expectedResult
            arguments("2023-02-07'T'20:00:00", "yyyyMMdd", null),
            arguments("2023-02-07", "yyyyMMdd", null),
            arguments("2023-02-07", "yyyyMM", null),
            arguments("20233112", "yyyyMMdd", null),
            arguments("233112", "yyMMdd", null),
            arguments("2331", "yyMM", null),
            arguments("20230207T20:00:00", "yyyyMMdd'T'HH:mm:ss", getDate(2023, 1, 7, 20, 0, 0)),
            arguments("20230207T20:00:00", "yyyyMMdd", getDate(2023, 1, 7)),
            arguments("230207T20:00:00", "yyMMdd", getDate(2023, 1, 7)),
            arguments("20230207", "yyyyMMdd", getDate(2023, 1, 7)),
            arguments("202302", "yyyyMM", getDate(2023, 1, 1)),
            arguments("230201", "yyMMdd", getDate(2023, 1, 1)),
            arguments("2302", "yyMM", getDate(2023, 1, 1)),
        )

        @JvmStatic
        @Suppress("LongParameterList")
        fun getDate(year: Int, month: Int, date: Int, hourOfDay: Int = 0, minute: Int = 0, second: Int = 0): Date =
            GregorianCalendar().apply {
                clear()
                set(year, month, date, hourOfDay, minute, second)
            }.time
    }
}
