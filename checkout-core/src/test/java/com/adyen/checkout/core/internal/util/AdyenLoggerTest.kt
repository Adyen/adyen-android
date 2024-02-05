/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/2/2024.
 */

package com.adyen.checkout.core.internal.util

import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.exception.CheckoutException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AdyenLoggerTest {

    private lateinit var logger: TestLogger

    @BeforeEach
    fun setup() {
        logger = TestLogger()
    }

    @Test
    fun `when logger is set, then logger instance is of correct type`() {
        AdyenLogger.setLogger(logger)

        assertEquals(logger, AdyenLogger.logger)
    }

    @Test
    fun `when logger is set, then logging is correctly propagated`() {
        AdyenLogger.setLogger(logger)
        val exception = CheckoutException("Test")

        adyenLog(AdyenLogLevel.INFO, exception) { "test" }

        val expected = LogData(AdyenLogLevel.INFO, "CO.AdyenLoggerTest", "test", exception)
        logger.assertLogCalled(expected)
    }

    @Test
    fun `when logger is reset, then logger is back to default type`() {
        AdyenLogger.setLogger(logger)

        AdyenLogger.resetLogger()

        assertInstanceOf(LogcatLogger::class.java, AdyenLogger.logger)
    }

    @Test
    fun `when log level is set, then it is correctly propagated`() {
        AdyenLogger.setLogger(logger)

        AdyenLogger.setLogLevel(AdyenLogLevel.ASSERT)

        logger.assertLogLevel(AdyenLogLevel.ASSERT)
    }

    private class TestLogger : AdyenLogger {

        private var logLevel: AdyenLogLevel = AdyenLogLevel.NONE

        private var lastLog: LogData? = null

        override fun shouldLog(level: AdyenLogLevel): Boolean = true

        override fun setLogLevel(level: AdyenLogLevel) {
            this.logLevel = level
        }

        override fun log(level: AdyenLogLevel, tag: String, message: String, throwable: Throwable?) {
            lastLog = LogData(level, tag, message, throwable)
        }

        fun assertLogLevel(expected: AdyenLogLevel) {
            assertEquals(expected, logLevel)
        }

        fun assertLogCalled(expected: LogData) {
            assertEquals(expected, lastLog)
        }
    }

    private data class LogData(
        val level: AdyenLogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )
}
