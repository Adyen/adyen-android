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
        // WHEN
        logAtAllLevels()

        // THEN
        assertEquals(LogLevel.OFF, AdyenLogDispatcher.logLevel)
        assertTrue(writer.logs.isEmpty())
    }

    @Test
    fun `when log levels are listed, then they are ordered from most to least verbose`() {
        // GIVEN
        val expected = listOf(
            LogLevel.VERBOSE,
            LogLevel.DEBUG,
            LogLevel.INFO,
            LogLevel.WARN,
            LogLevel.ERROR,
            LogLevel.ASSERT,
            LogLevel.OFF,
        )

        // WHEN
        val levels = LogLevel.entries

        // THEN
        assertEquals(expected, levels)
    }

    @ParameterizedTest
    @MethodSource("writtenLevelsSource")
    fun `when log level is set, then only messages at that level and above are written`(
        level: LogLevel,
        expectedLevels: List<LogLevel>,
    ) {
        // GIVEN
        AdyenLogger.setLogLevel(level)

        // WHEN
        logAtAllLevels()

        // THEN
        assertEquals(expectedLevels, writer.logs.map { it.level })
    }

    @Test
    fun `when a message is logged at OFF, then it is never written`() {
        // GIVEN
        AdyenLogger.setLogLevel(LogLevel.VERBOSE)

        // WHEN
        adyenLog(LogLevel.OFF) { "test" }
        adyenLog(LogLevel.OFF, "Tag") { "test" }

        // THEN
        assertTrue(writer.logs.isEmpty())
    }

    @Test
    fun `when the writer is replaced, then the log level is kept`() {
        // GIVEN
        AdyenLogger.setLogLevel(LogLevel.WARN)
        val newWriter = RecordingLogWriter()

        // WHEN
        AdyenLogDispatcher.writer = newWriter
        adyenLog(LogLevel.INFO) { "info" }
        adyenLog(LogLevel.WARN) { "warn" }

        // THEN
        assertEquals(LogLevel.WARN, AdyenLogDispatcher.logLevel)
        assertEquals(listOf(LogLevel.WARN), newWriter.logs.map { it.level })
    }

    @Test
    fun `when a message is filtered out, then its message is not built`() {
        // GIVEN
        AdyenLogger.setLogLevel(LogLevel.ERROR)
        var isEvaluated = false

        // WHEN
        adyenLog(LogLevel.DEBUG) {
            isEvaluated = true
            "test"
        }
        adyenLog(LogLevel.DEBUG, "Tag") {
            isEvaluated = true
            "test"
        }

        // THEN
        assertFalse(isEvaluated)
    }

    @Test
    fun `when a message is logged, then it is written with the outer class name as tag`() {
        // GIVEN
        AdyenLogger.setLogLevel(LogLevel.VERBOSE)
        val exception = RuntimeException("Test")

        // WHEN
        adyenLog(LogLevel.INFO, exception) { "test" }

        // THEN
        val expected = LogData(LogLevel.INFO, "CO.AdyenLoggerTest", "test", exception)
        assertEquals(listOf(expected), writer.logs)
    }

    @Test
    fun `when a message is logged with a tag, then it is written with the prefixed tag`() {
        // GIVEN
        AdyenLogger.setLogLevel(LogLevel.VERBOSE)
        val exception = RuntimeException("Test")

        // WHEN
        adyenLog(LogLevel.ERROR, "SomeTag", exception) { "test" }

        // THEN
        val expected = LogData(LogLevel.ERROR, "CO.SomeTag", "test", exception)
        assertEquals(listOf(expected), writer.logs)
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
