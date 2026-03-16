/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.input.TextFieldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SocialSecurityNumberOutputTransformationTest {

    private val outputTransformation = SocialSecurityNumberOutputTransformation()

    @ParameterizedTest
    @MethodSource("socialSecurityNumberSource")
    fun `when outputting social security number then it is formatted correctly`(
        rawText: String,
        formattedText: String
    ) {
        val state = TextFieldState(rawText)
        state.edit {
            with(outputTransformation) { transformOutput() }
        }

        assertEquals(formattedText, state.text.toString())
    }

    companion object {
        @JvmStatic
        fun socialSecurityNumberSource() = listOf(
            // rawText, formattedText
            arguments("", ""),
            arguments("1", "1"),
            arguments("12", "12"),
            arguments("123", "123"),
            arguments("1234", "123.4"),
            arguments("12345", "123.45"),
            arguments("123456", "123.456"),
            arguments("1234567", "123.456.7"),
            arguments("12345678", "123.456.78"),
            arguments("123456789", "123.456.789"),
            arguments("1234567890", "123.456.789-0"),
            arguments("12345678901", "123.456.789-01"),
            arguments("123456789012", "12.345.678/9012"),
            arguments("1234567890123", "12.345.678/9012-3"),
            arguments("12345678901234", "12.345.678/9012-34"),
        )
    }
}
