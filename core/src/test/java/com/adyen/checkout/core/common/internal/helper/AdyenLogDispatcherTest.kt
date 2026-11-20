/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutLogLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AdyenLogDispatcherTest {

    private lateinit var originalWriter: LogWriter
    private lateinit var originalLevel: CheckoutLogLevel
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
        assertEquals(CheckoutLogLevel.OFF, AdyenLogDispatcher.logLevel)
        assertTrue(writer.logs.isEmpty())
    }

    @Test
    fun `when log levels are listed, then they are ordered from most to least verbose`() {
        // GIVEN
        val expected = listOf(
            CheckoutLogLevel.VERBOSE,
            CheckoutLogLevel.DEBUG,
            CheckoutLogLevel.INFO,
            CheckoutLogLevel.WARN,
            CheckoutLogLevel.ERROR,
            CheckoutLogLevel.ASSERT,
            CheckoutLogLevel.OFF,
        )

        // WHEN
        val levels = CheckoutLogLevel.entries

        // THEN
        assertEquals(expected, levels)
    }

    @ParameterizedTest
    @MethodSource("writtenLevelsSource")
    fun `when log level is set, then only messages at that level and above are written`(
        level: CheckoutLogLevel,
        expectedLevels: List<CheckoutLogLevel>,
    ) {
        // GIVEN
        Checkout.setLogLevel(level)

        // WHEN
        logAtAllLevels()

        // THEN
        assertEquals(expectedLevels, writer.logs.map { it.level })
    }

    @Test
    fun `when a message is logged at OFF, then it is never written`() {
        // GIVEN
        Checkout.setLogLevel(CheckoutLogLevel.VERBOSE)

        // WHEN
        adyenLog(CheckoutLogLevel.OFF) { "test" }
        adyenLog(CheckoutLogLevel.OFF, "Tag") { "test" }

        // THEN
        assertTrue(writer.logs.isEmpty())
    }

    @Test
    fun `when the writer is replaced, then the log level is kept`() {
        // GIVEN
        Checkout.setLogLevel(CheckoutLogLevel.WARN)
        val newWriter = RecordingLogWriter()

        // WHEN
        AdyenLogDispatcher.writer = newWriter
        adyenLog(CheckoutLogLevel.INFO) { "info" }
        adyenLog(CheckoutLogLevel.WARN) { "warn" }

        // THEN
        assertEquals(CheckoutLogLevel.WARN, AdyenLogDispatcher.logLevel)
        assertEquals(listOf(CheckoutLogLevel.WARN), newWriter.logs.map { it.level })
    }

    @Test
    fun `when a message is filtered out, then its message is not built`() {
        // GIVEN
        Checkout.setLogLevel(CheckoutLogLevel.ERROR)
        var isEvaluated = false

        // WHEN
        adyenLog(CheckoutLogLevel.DEBUG) {
            isEvaluated = true
            "test"
        }
        adyenLog(CheckoutLogLevel.DEBUG, "Tag") {
            isEvaluated = true
            "test"
        }

        // THEN
        assertFalse(isEvaluated)
    }

    @Test
    fun `when a message is logged, then it is written with the outer class name as tag`() {
        // GIVEN
        Checkout.setLogLevel(CheckoutLogLevel.VERBOSE)
        val exception = RuntimeException("Test")

        // WHEN
        adyenLog(CheckoutLogLevel.INFO, exception) { "test" }

        // THEN
        val expected = LogData(CheckoutLogLevel.INFO, "CO.AdyenLogDispatcherTest", "test", exception)
        assertEquals(listOf(expected), writer.logs)
    }

    @Test
    fun `when a message is logged with a tag, then it is written with the prefixed tag`() {
        // GIVEN
        Checkout.setLogLevel(CheckoutLogLevel.VERBOSE)
        val exception = RuntimeException("Test")

        // WHEN
        adyenLog(CheckoutLogLevel.ERROR, "SomeTag", exception) { "test" }

        // THEN
        val expected = LogData(CheckoutLogLevel.ERROR, "CO.SomeTag", "test", exception)
        assertEquals(listOf(expected), writer.logs)
    }

    private fun logAtAllLevels() {
        CheckoutLogLevel.entries
            .filter { it != CheckoutLogLevel.OFF }
            .forEach { level -> adyenLog(level) { level.name } }
    }

    private class RecordingLogWriter : LogWriter {

        val logs = mutableListOf<LogData>()

        override fun write(level: CheckoutLogLevel, tag: String, message: String, throwable: Throwable?) {
            logs.add(LogData(level, tag, message, throwable))
        }
    }

    private data class LogData(
        val level: CheckoutLogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )

    companion object {

        @JvmStatic
        fun writtenLevelsSource() = listOf(
            arguments(
                CheckoutLogLevel.VERBOSE,
                listOf(
                    CheckoutLogLevel.VERBOSE,
                    CheckoutLogLevel.DEBUG,
                    CheckoutLogLevel.INFO,
                    CheckoutLogLevel.WARN,
                    CheckoutLogLevel.ERROR,
                    CheckoutLogLevel.ASSERT,
                ),
            ),
            arguments(
                CheckoutLogLevel.DEBUG,
                listOf(
                    CheckoutLogLevel.DEBUG,
                    CheckoutLogLevel.INFO,
                    CheckoutLogLevel.WARN,
                    CheckoutLogLevel.ERROR,
                    CheckoutLogLevel.ASSERT,
                ),
            ),
            arguments(
                CheckoutLogLevel.INFO,
                listOf(CheckoutLogLevel.INFO, CheckoutLogLevel.WARN, CheckoutLogLevel.ERROR, CheckoutLogLevel.ASSERT),
            ),
            arguments(
                CheckoutLogLevel.WARN,
                listOf(CheckoutLogLevel.WARN, CheckoutLogLevel.ERROR, CheckoutLogLevel.ASSERT),
            ),
            arguments(CheckoutLogLevel.ERROR, listOf(CheckoutLogLevel.ERROR, CheckoutLogLevel.ASSERT)),
            arguments(CheckoutLogLevel.ASSERT, listOf(CheckoutLogLevel.ASSERT)),
            arguments(CheckoutLogLevel.OFF, emptyList<CheckoutLogLevel>()),
        )
    }
}
