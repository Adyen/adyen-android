/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import android.util.Log
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.AdyenLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments.arguments

internal class AdyenLoggerTest {

    private lateinit var logger: TestLogger

    @BeforeEach
    fun setup() {
        AdyenLogger.resetLogger()
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
        AdyenLogger.setLogLevel(AdyenLogLevel.VERBOSE)

        // TODO - Error Propagation
        val exception = RuntimeException("Test")

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

    @Test
    fun `when log level is set, then log is not executed`() {
        AdyenLogger.setLogger(logger)
        AdyenLogger.setLogLevel(AdyenLogLevel.ERROR)

        adyenLog(AdyenLogLevel.VERBOSE) { "test" }

        logger.assertLogNotCalled()
    }

    @Test
    fun `when log level is set manually, then setting the initial level has no effect`() {
        AdyenLogger.setLogger(logger)
        AdyenLogger.setLogLevel(AdyenLogLevel.ASSERT)

        AdyenLogger.setInitialLogLevel(AdyenLogLevel.ERROR)

        logger.assertLogLevel(AdyenLogLevel.ASSERT)
    }

    @Test
    fun `when log level is not set manually, then setting the initial level has effect`() {
        AdyenLogger.setLogger(logger)

        AdyenLogger.setInitialLogLevel(AdyenLogLevel.ERROR)

        logger.assertLogLevel(AdyenLogLevel.ERROR)
    }

    private class TestLogger : AdyenLogger {

        private var minLogLevel: AdyenLogLevel = AdyenLogLevel.NONE

        private var lastLog: LogData? = null

        override fun shouldLog(level: AdyenLogLevel): Boolean {
            return level.priority >= minLogLevel.priority
        }

        override fun setLogLevel(level: AdyenLogLevel) {
            this.minLogLevel = level
        }

        override fun log(level: AdyenLogLevel, tag: String, message: String, throwable: Throwable?) {
            lastLog = LogData(level, tag, message, throwable)
        }

        fun assertLogLevel(expected: AdyenLogLevel) {
            assertEquals(expected, minLogLevel)
        }

        fun assertLogCalled(expected: LogData) {
            assertEquals(expected, lastLog)
        }

        fun assertLogNotCalled() {
            assertNull(lastLog)
        }
    }

    private data class LogData(
        val level: AdyenLogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )

    companion object {

        @Suppress("DEPRECATION")
        @JvmStatic
        fun logLevelSource() = listOf(
            arguments(Log.VERBOSE, AdyenLogLevel.VERBOSE),
            arguments(Log.DEBUG, AdyenLogLevel.DEBUG),
            arguments(Log.INFO, AdyenLogLevel.INFO),
            arguments(Log.WARN, AdyenLogLevel.WARN),
            arguments(Log.ERROR, AdyenLogLevel.ERROR),
            arguments(Log.ASSERT, AdyenLogLevel.NONE),
        )
    }
}
