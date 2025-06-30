package com.adyen.checkout.core.ui.model

import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.internal.ui.model.INVALID_DATE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ExpiryDateTest {

    @ParameterizedTest
    @MethodSource("toMMyyFormatSource")
    fun `expiry date is converted to MMyy format`(
        expiryDate: ExpiryDate,
        expected: String,
    ) {
        assertEquals(expected, expiryDate.toMMyyString())
    }

    @ParameterizedTest
    @MethodSource("createExpiryDateFromStringSource")
    fun `expiry date is created from a string`(
        expiryDate: String,
        expected: ExpiryDate,
    ) {
        val actual = ExpiryDate.from(expiryDate)

        assertEquals(expected, actual)
    }

    companion object {

        @JvmStatic
        fun toMMyyFormatSource() = listOf(
            arguments(ExpiryDate(1, 1), "01/01"),
            arguments(ExpiryDate(12, 1234), "12/34"),
            arguments(ExpiryDate(9, 2008), "09/08"),
            arguments(EMPTY_DATE, "00/00"),
            arguments(INVALID_DATE, "-1/-1"),
        )

        @JvmStatic
        fun createExpiryDateFromStringSource() = listOf(
            arguments("01/01", ExpiryDate(1, 1)),
            arguments("3/3", ExpiryDate(3, 3)),
            arguments("03/3", ExpiryDate(3, 3)),
            arguments("3/03", ExpiryDate(3, 3)),
            arguments("12/34", ExpiryDate(12, 34)),
            arguments("09/08", ExpiryDate(9, 8)),
            arguments("00/00", ExpiryDate(0, 0)),
            arguments("-9/-18", ExpiryDate(-9, -18)),
            // Invalid input should create invalid expiry date
            arguments("03", ExpiryDate(3, -1)),
            arguments("asd/fgh", ExpiryDate(-1, -1)),
            arguments("asdfgh", ExpiryDate(-1, -1)),
        )
    }
}
