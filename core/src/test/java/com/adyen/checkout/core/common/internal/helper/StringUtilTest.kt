/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StringUtilTest {

    @Test
    fun `when value has spaces and specified character - remove all spaces and character`() {
        val initial = "1234  0056"
        val expected = "123456"
        val result = StringUtil.normalize(initial, '0')
        println("Result: $result")
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource("digitsAndSeparatorsSource")
    fun `when value is digits and separators only - return true, else false`(
        value: String,
        separators: CharArray,
        expected: Boolean,
    ) {
        val result = StringUtil.isDigitsAndSeparatorsOnly(value, *separators)
        assertEquals(expected, result)
    }

    companion object {

        @JvmStatic
        fun digitsAndSeparatorsSource() = listOf(
            arguments(
                "1234 5678",
                charArrayOf(' '),
                true,
            ),
            arguments(
                "12/34",
                charArrayOf('/'),
                true,
            ),
            arguments(
                "12/34 56",
                charArrayOf('/', ' '),
                true,
            ),
            arguments(
                "12/34",
                charArrayOf(' '),
                false,
            ),
            arguments(
                "1234. ",
                charArrayOf(' '),
                false,
            ),
        )
    }
}
