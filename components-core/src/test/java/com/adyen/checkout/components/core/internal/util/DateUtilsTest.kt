/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/1/2024.
 */

package com.adyen.checkout.components.core.internal.util

import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.internal.util.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Calendar
import java.util.Locale

class DateUtilsTest {

    @BeforeEach
    fun beforeEach() {
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @ParameterizedTest
    @MethodSource("parseDateToView")
    fun testParseDateToView(month: String, year: String, expectedFormattedDate: String) {
        val formattedDate = DateUtils.parseDateToView(month, year)

        assertEquals(expectedFormattedDate, formattedDate)
    }

    @ParameterizedTest
    @MethodSource("toServerDateFormat")
    fun testToServerDateFormat(calendar: Calendar, expectedFormattedDate: String) {
        val formattedDate = DateUtils.toServerDateFormat(calendar)

        assertEquals(expectedFormattedDate, formattedDate)
    }

    @ParameterizedTest
    @MethodSource("matchesFormat")
    fun testMatchesFormat(date: String, format: String, expectedResult: Boolean) {
        val result = DateUtils.matchesFormat(date, format)

        assertEquals(expectedResult, result)
    }

    @ParameterizedTest
    @MethodSource("formatStringDate")
    fun testFormatStringDate(date: String, shopperLocale: Locale, inputFormat: String, expectedFormattedDate: String?) {
        val formattedDate = DateUtils.formatStringDate(date, shopperLocale, inputFormat)

        assertEquals(expectedFormattedDate, formattedDate)
    }

    @ParameterizedTest
    @MethodSource("formatDateToString")
    fun testFormatDateToString(calendar: Calendar, pattern: String, expectedFormattedDate: String?) {
        val formattedDate = DateUtils.formatDateToString(calendar, pattern)

        assertEquals(expectedFormattedDate, formattedDate)
    }

    companion object {
        @JvmStatic
        fun parseDateToView() = listOf(
            // month, year, expectedFormattedDate
            arguments("1", "2021", "1/21"),
            arguments("02", "2020", "02/20"),
            arguments("12", "2023", "12/23"),
        )

        @JvmStatic
        fun toServerDateFormat() = listOf(
            // date, expectedFormattedDate
            arguments(getCalendar(2023, 1, 7), "2023-02-07"),
            arguments(getCalendar(2024, 0, 1), "2024-01-01"),
            arguments(getCalendar(2024, 11, 1), "2024-12-01"),
            arguments(getCalendar(2019, 0, 1, 0, 0, 0), "2019-01-01"),
            arguments(getCalendar(2019, 0, 1, 20, 30, 30), "2019-01-01"),
        )

        @JvmStatic
        fun matchesFormat() = listOf(
            // date, format, expectedResult
            arguments("2023-02-07'T'20:00:00", "yyyyMMdd", false),
            arguments("2023-02-07", "yyyyMMdd", false),
            arguments("2023-02-07", "yyyyMM", false),
            arguments("20233112", "yyyyMMdd", false),
            arguments("233112", "yyMMdd", false),
            arguments("2331", "yyMM", false),
            arguments("20230207T20:00:00", "yyyyMMdd'T'HH:mm:ss", true),
            arguments("20230207T20:00:00", "yyyyMMdd", true),
            arguments("230207T20:00:00", "yyMMdd", true),
            arguments("20230207", "yyyyMMdd", true),
            arguments("202302", "yyyyMM", true),
            arguments("230201", "yyMMdd", true),
            arguments("2302", "yyMM", true),
        )

        @JvmStatic
        fun formatStringDate() = listOf(
            // date, shopperLocale, inputFormat, expectedResult
            arguments("2023-02-07'T'20:00:00", Locale.US, "yyyy-MM-dd'T'HH:mm:ss", null),
            arguments("2024-02-01", Locale.US, "yyyy-MM-dd", "2/1/24"),
            arguments("230207", Locale.US, "yyMMdd", "2/7/23"),
            arguments("231302", Locale.US, "yyMMdd", "1/2/24"),
            arguments("2024-02-01", Locale.UK, "yy-MM-dd", "01/02/2024"),
            arguments("231302", Locale.UK, "yyMMdd", "02/01/2024"),
            arguments("2024-02-01", Locale.JAPAN, "yy-MM-dd", "2024/02/01"),
            arguments("2024-02-01", Locale.CHINA, "yy-MM-dd", "2024/2/1"),
            arguments("2024-02-01", Locale.CANADA, "yy-MM-dd", "2024-02-01"),
            arguments("2024-02-01", Locale.FRANCE, "yy-MM-dd", "01/02/2024"),
        )

        @JvmStatic
        fun formatDateToString() = listOf(
            // date, pattern, expectedFormattedDate
            arguments(getCalendar(2024, 0, 5, 20, 10, 5), "yyyy-MM-dd'T'HH:mm:ss", "2024-01-05T20:10:05"),
            arguments(getCalendar(2020, 11, 31, 23, 59, 59), "yyyy-MM-dd'T'HH:mm:ss", "2020-12-31T23:59:59"),
            arguments(getCalendar(2019, 0, 1, 0, 0, 0), "yyyy-MM-dd'T'HH:mm:ss", "2019-01-01T00:00:00"),
            arguments(getCalendar(2019, 0, 1, 0, 0, 0), "yyyyMMdd'T'HHmmss", "20190101T000000"),
            arguments(getCalendar(2019, 1, 1, 0, 0, 0), "yyyyMMdd", "20190201"),
            arguments(getCalendar(2024, 0, 5, 20, 10, 5), "xxxxxxxxxxxx", null),
        )

        @JvmStatic
        fun getCalendar(year: Int, month: Int, date: Int): Calendar =
            Calendar.getInstance().apply {
                set(year, month, date)
            }

        @JvmStatic
        @Suppress("LongParameterList")
        fun getCalendar(year: Int, month: Int, date: Int, hourOfDay: Int, minute: Int, second: Int): Calendar =
            Calendar.getInstance().apply {
                set(year, month, date, hourOfDay, minute, second)
            }
    }
}
