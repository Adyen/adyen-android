/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/1/2023.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.old.internal.util.CardNumberUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CardNumberUtilsTest {

    @ParameterizedTest
    @MethodSource("formattedCardNumbersSource")
    fun `when formatCardNumber is called then the correct format should be produced`(
        unformattedString: String,
        maskPartsLengths: List<Int>,
        separator: String,
        expectedResult: String,
    ) {
        val formattedNumber = CardNumberUtils.formatCardNumber(unformattedString, maskPartsLengths, separator)
        assertEquals(expectedResult, formattedNumber)
    }

    companion object {

        @JvmStatic
        fun formattedCardNumbersSource() = listOf(
            arguments("", DEFAULT_CARD_NUMBER_MASK, DEFAULT_SEPARATOR, ""),
            arguments("1", DEFAULT_CARD_NUMBER_MASK, DEFAULT_SEPARATOR, "1"),
            arguments("1234", DEFAULT_CARD_NUMBER_MASK, DEFAULT_SEPARATOR, "1234"),
            arguments("12345", DEFAULT_CARD_NUMBER_MASK, DEFAULT_SEPARATOR, "1234 5"),
            arguments("12345678", DEFAULT_CARD_NUMBER_MASK, DEFAULT_SEPARATOR, "1234 5678"),
            arguments("1762718349267126174", DEFAULT_CARD_NUMBER_MASK, DEFAULT_SEPARATOR, "1762 7183 4926 7126 174"),
            arguments(
                "98547843658478574847512332",
                DEFAULT_CARD_NUMBER_MASK,
                DEFAULT_SEPARATOR,
                "9854 7843 6584 7857 484 7512332"
            ),
            arguments("1298347436736473489", listOf(4, 6, 5, 4), DEFAULT_SEPARATOR, "1298 347436 73647 3489"),
            arguments("12334556", DEFAULT_CARD_NUMBER_MASK, "-", "1233-4556"),
            arguments("123234", emptyList<Int>(), DEFAULT_SEPARATOR, "123234"),
        )

        private val DEFAULT_CARD_NUMBER_MASK = listOf(4, 4, 4, 4, 3)
        private const val DEFAULT_SEPARATOR = " "
    }
}
