/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 11/11/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import android.util.Log
import com.adyen.checkout.core.common.AdyenLogger.LogLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class LogcatLoggerTest {

    @ParameterizedTest
    @MethodSource("logcatPrioritySource")
    fun `when a log level is mapped, then the matching Logcat priority is returned`(
        level: LogLevel,
        expectedPriority: Int?,
    ) {
        // WHEN
        val priority = level.toLogcatPriority()

        // THEN
        assertEquals(expectedPriority, priority)
    }

    companion object {

        @JvmStatic
        fun logcatPrioritySource() = listOf(
            arguments(LogLevel.VERBOSE, Log.VERBOSE),
            arguments(LogLevel.DEBUG, Log.DEBUG),
            arguments(LogLevel.INFO, Log.INFO),
            arguments(LogLevel.WARN, Log.WARN),
            arguments(LogLevel.ERROR, Log.ERROR),
            arguments(LogLevel.ASSERT, Log.ASSERT),
            arguments(LogLevel.OFF, null),
        )
    }
}
