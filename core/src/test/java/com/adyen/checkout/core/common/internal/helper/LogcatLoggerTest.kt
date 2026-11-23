/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 11/11/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import android.util.Log
import com.adyen.checkout.core.components.CheckoutLogLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class LogcatLoggerTest {

    @ParameterizedTest
    @MethodSource("logcatPrioritySource")
    fun `when a log level is mapped, then the matching Logcat priority is returned`(
        level: CheckoutLogLevel,
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
            arguments(CheckoutLogLevel.VERBOSE, Log.VERBOSE),
            arguments(CheckoutLogLevel.DEBUG, Log.DEBUG),
            arguments(CheckoutLogLevel.INFO, Log.INFO),
            arguments(CheckoutLogLevel.WARN, Log.WARN),
            arguments(CheckoutLogLevel.ERROR, Log.ERROR),
            arguments(CheckoutLogLevel.ASSERT, Log.ASSERT),
            arguments(CheckoutLogLevel.OFF, null),
        )
    }
}
