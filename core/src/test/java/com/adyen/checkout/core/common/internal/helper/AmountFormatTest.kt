/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.components.data.model.Amount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AmountFormatTest {

    @Test
    fun `when currency is a supported Adyen currency then Adyen fraction digits are used`() {
        val amount = Amount(currency = "JPY", value = 1337L)

        val result = AmountFormat.toBigDecimal(amount)

        assertEquals(BigDecimal.valueOf(1337L, 0), result)
    }

    @Test
    fun `when currency is CNH then two fraction digits are used`() {
        // CNH (offshore Chinese yuan) is a real Adyen-supported currency, but is not recognized by
        // java.util.Currency. It must be resolved through the Adyen CheckoutCurrency table, not the
        // platform fallback, otherwise it silently falls back to zero fraction digits.
        val amount = Amount(currency = "CNH", value = 1337L)

        val result = AmountFormat.toBigDecimal(amount)

        assertEquals(BigDecimal.valueOf(1337L, 2), result)
    }

    @Test
    fun `when currency is not an Adyen currency but is recognized by the platform then platform fraction digits are used`() {
        // CLF is not an Adyen-supported currency, but the platform recognizes it with 4 fraction digits.
        val amount = Amount(currency = "CLF", value = 1337L)

        val result = AmountFormat.toBigDecimal(amount)

        assertEquals(BigDecimal.valueOf(1337L, 4), result)
    }

    @Test
    fun `when currency is not recognized at all then zero fraction digits are used`() {
        val amount = Amount(currency = "TEST", value = 1337L)

        val result = AmountFormat.toBigDecimal(amount)

        assertEquals(BigDecimal.valueOf(1337L, 0), result)
    }
}
