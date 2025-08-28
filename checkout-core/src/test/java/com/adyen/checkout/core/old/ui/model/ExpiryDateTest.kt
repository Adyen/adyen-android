package com.adyen.checkout.core.old.ui.model

import com.adyen.checkout.core.old.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.old.internal.ui.model.INVALID_DATE
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
            arguments(ExpiryDate(10, 1998), "10/98"),
            arguments(ExpiryDate(10, 2098), "10/98"),
            arguments(EMPTY_DATE, "00/00"),
            arguments(INVALID_DATE, "-1/-1"),
        )

        @JvmStatic
        fun createExpiryDateFromStringSource() = listOf(
            arguments("01/01", ExpiryDate(1, 2001)),
            arguments("3/03", ExpiryDate(3, 2003)),
            arguments("12/34", ExpiryDate(12, 2034)),
            arguments("09/08", ExpiryDate(9, 2008)),
            arguments("01/00", ExpiryDate(1, 2000)),
            // Our correction adds 100 years when the date parsing gives a smaller century than the current
            arguments("10/98", ExpiryDate(10, 2098)),
            arguments("3/3", ExpiryDate(3, 103)),
            arguments("03/3", ExpiryDate(3, 103)),
            // Invalid input should create invalid expiry date
            arguments("00/00", INVALID_DATE), // Month 0 doesn't exist
            arguments("03", INVALID_DATE),
            arguments("-9/-18", INVALID_DATE),
            arguments("asd/fgh", INVALID_DATE),
            arguments("asdfgh", INVALID_DATE),
        )
    }
}
