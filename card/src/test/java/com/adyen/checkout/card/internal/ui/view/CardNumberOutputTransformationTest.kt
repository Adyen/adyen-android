/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.input.TextFieldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

class CardNumberOutputTransformationTest {

    @ParameterizedTest
    @MethodSource("defaultCardNumberSource")
    fun `when outputting default card number then it is formatted correctly`(
        rawText: String,
        formattedText: String
    ) {
        val outputTransformation = CardNumberOutputTransformation(isAmex = false)
        val state = TextFieldState(rawText)
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals(formattedText, state.text.toString())
    }

    @ParameterizedTest
    @MethodSource("amexCardNumberSource")
    fun `when outputting amex card number then it is formatted correctly`(
        rawText: String,
        formattedText: String
    ) {
        val outputTransformation = CardNumberOutputTransformation(isAmex = true)
        val state = TextFieldState(rawText)
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals(formattedText, state.text.toString())
    }

    companion object {
        @JvmStatic
        fun defaultCardNumberSource() = listOf(
            // rawText, formattedText
            arguments("", ""),
            arguments("1", "1"),
            arguments("12", "12"),
            arguments("123", "123"),
            arguments("1234", "1234"),
            arguments("12345", "1234 5"),
            arguments("123456", "1234 56"),
            arguments("1234567", "1234 567"),
            arguments("12345678", "1234 5678"),
            arguments("123456789", "1234 5678 9"),
            arguments("1234567890", "1234 5678 90"),
            arguments("12345678901", "1234 5678 901"),
            arguments("123456789012", "1234 5678 9012"),
            arguments("1234567890123", "1234 5678 9012 3"),
            arguments("12345678901234", "1234 5678 9012 34"),
            arguments("123456789012345", "1234 5678 9012 345"),
            arguments("1234567890123456", "1234 5678 9012 3456"),
            arguments("12345678901234567", "1234 5678 9012 3456 7"),
            arguments("123456789012345678", "1234 5678 9012 3456 78"),
            arguments("1234567890123456789", "1234 5678 9012 3456 789"),
        )

        @JvmStatic
        fun amexCardNumberSource() = listOf(
            // rawText, formattedText
            arguments("", ""),
            arguments("1", "1"),
            arguments("12", "12"),
            arguments("123", "123"),
            arguments("1234", "1234"),
            arguments("12341", "1234 1"),
            arguments("123412", "1234 12"),
            arguments("1234123", "1234 123"),
            arguments("12341234", "1234 1234"),
            arguments("123412345", "1234 12345"),
            arguments("1234123456", "1234 123456"),
            arguments("12341234561", "1234 123456 1"),
            arguments("123412345612", "1234 123456 12"),
            arguments("1234123456123", "1234 123456 123"),
            arguments("12341234561234", "1234 123456 1234"),
            arguments("123412345612345", "1234 123456 12345"),
            arguments("1234123456123451", "1234 123456 12345 1"),
            arguments("12341234561234512", "1234 123456 12345 12"),
            arguments("123412345612345123", "1234 123456 12345 123"),
            arguments("1234123456123451234", "1234 123456 12345 1234"),
        )
    }
}
