/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.common.AdyenLogger
import com.adyen.checkout.core.common.AdyenLogger.LogLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AdyenLoggerTest {

    private lateinit var originalWriter: LogWriter
    private lateinit var originalLevel: LogLevel
    private lateinit var writer: RecordingLogWriter

    @BeforeEach
    fun setup() {
        originalWriter = AdyenLogDispatcher.writer
        originalLevel = AdyenLogDispatcher.logLevel
        writer = RecordingLogWriter()
        AdyenLogDispatcher.writer = writer
    }

    @AfterEach
    fun tearDown() {
        AdyenLogDispatcher.writer = originalWriter
        AdyenLogDispatcher.logLevel = originalLevel
    }

    @Test
    fun `when no level is set, then nothing is written`() {
        logAtAllLevels()

        assertEquals(LogLevel.OFF, AdyenLogDispatcher.logLevel)
        assertTrue(writer.logs.isEmpty())
    }

    @Test
    fun `when log levels are listed, then they are ordered from most to least verbose`() {
        val expected = listOf(
            LogLevel.VERBOSE,
            LogLevel.DEBUG,
            LogLevel.INFO,
            LogLevel.WARN,
            LogLevel.ERROR,
            LogLevel.ASSERT,
            LogLevel.OFF,
        )

        assertEquals(expected, LogLevel.entries)
    }

    @ParameterizedTest
    @MethodSource("writtenLevelsSource")
    fun `when log level is set, then only messages at that level and above are written`(
        level: LogLevel,
        expectedLevels: List<LogLevel>,
    ) {
        AdyenLogger.setLogLevel(level)

        logAtAllLevels()

        assertEquals(expectedLevels, writer.logs.map { it.level })
    }

    @Test
    fun `when a message is logged at OFF, then it is never written`() {
        AdyenLogger.setLogLevel(LogLevel.VERBOSE)

        adyenLog(LogLevel.OFF) { "test" }
        adyenLog(LogLevel.OFF, "Tag") { "test" }

        assertTrue(writer.logs.isEmpty())
    }

    @Test
    fun `when the writer is replaced, then the log level is kept`() {
        AdyenLogger.setLogLevel(LogLevel.WARN)
        val newWriter = RecordingLogWriter()

        AdyenLogDispatcher.writer = newWriter
        adyenLog(LogLevel.INFO) { "info" }
        adyenLog(LogLevel.WARN) { "warn" }

        assertEquals(LogLevel.WARN, AdyenLogDispatcher.logLevel)
        assertEquals(listOf(LogLevel.WARN), newWriter.logs.map { it.level })
    }

    @Test
    fun `when a message is filtered out, then its message is not built`() {
        AdyenLogger.setLogLevel(LogLevel.ERROR)
        var isEvaluated = false

        adyenLog(LogLevel.DEBUG) {
            isEvaluated = true
            "test"
        }
        adyenLog(LogLevel.DEBUG, "Tag") {
            isEvaluated = true
            "test"
        }

        assertFalse(isEvaluated)
    }

    @Test
    fun `when a message is logged, then it is written with the outer class name as tag`() {
        AdyenLogger.setLogLevel(LogLevel.VERBOSE)
        val exception = RuntimeException("Test")

        adyenLog(LogLevel.INFO, exception) { "test" }

        val expected = LogData(LogLevel.INFO, "CO.AdyenLoggerTest", "test", exception)
        assertEquals(listOf(expected), writer.logs)
    }

    @Test
    fun `when a message is logged with a tag, then it is written with the prefixed tag`() {
        AdyenLogger.setLogLevel(LogLevel.VERBOSE)
        val exception = RuntimeException("Test")

        adyenLog(LogLevel.ERROR, "SomeTag", exception) { "test" }

        val expected = LogData(LogLevel.ERROR, "CO.SomeTag", "test", exception)
        assertEquals(listOf(expected), writer.logs)
    }

    @Test
    fun `when log level is set, then the dispatcher uses it`() {
        AdyenLogger.setLogLevel(LogLevel.DEBUG)

        assertEquals(LogLevel.DEBUG, AdyenLogDispatcher.logLevel)
    }

    private fun logAtAllLevels() {
        LogLevel.entries
            .filter { it != LogLevel.OFF }
            .forEach { level -> adyenLog(level) { level.name } }
    }

    private class RecordingLogWriter : LogWriter {

        val logs = mutableListOf<LogData>()

        override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
            logs.add(LogData(level, tag, message, throwable))
        }
    }

    private data class LogData(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )

    companion object {

        @JvmStatic
        fun writtenLevelsSource() = listOf(
            arguments(
                LogLevel.VERBOSE,
                listOf(LogLevel.VERBOSE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.ASSERT),
            ),
            arguments(
                LogLevel.DEBUG,
                listOf(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.ASSERT),
            ),
            arguments(LogLevel.INFO, listOf(LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR, LogLevel.ASSERT)),
            arguments(LogLevel.WARN, listOf(LogLevel.WARN, LogLevel.ERROR, LogLevel.ASSERT)),
            arguments(LogLevel.ERROR, listOf(LogLevel.ERROR, LogLevel.ASSERT)),
            arguments(LogLevel.ASSERT, listOf(LogLevel.ASSERT)),
            arguments(LogLevel.OFF, emptyList<LogLevel>()),
        )
    }
}
