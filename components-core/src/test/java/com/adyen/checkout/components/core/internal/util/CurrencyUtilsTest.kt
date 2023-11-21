/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/11/2023.
 */

package com.adyen.checkout.components.core.internal.util

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.exception.CheckoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Locale

internal class CurrencyUtilsTest {

    @Test
    fun `format amount with nl-NL locale`() {
        val amount = Amount("EUR", 10050L)
        val locale = Locale.forLanguageTag("nl-NL")

        val formattedAmount = CurrencyUtils.formatAmount(amount, locale)

        assertEquals("€ 100,50", formattedAmount)
    }

    @Test
    fun `format amount with ar-LB locale`() {
        val amount = Amount("LBP", 10050L)
        val locale = Locale.forLanguageTag("ar-LB")

        val formattedAmount = CurrencyUtils.formatAmount(amount, locale)

        assertEquals("ل.ل.\u200F ١٠٠٫٥٠", formattedAmount)
    }

    @Test
    fun `format amount with en-US locale`() {
        val amount = Amount("USD", 10050L)
        val locale = Locale.forLanguageTag("en-US")

        val formattedAmount = CurrencyUtils.formatAmount(amount, locale)

        assertEquals("$100.50", formattedAmount)
    }

    @Test
    fun `assert currency does nothing, if currency code is supported`() {
        val currencyCode = "EUR"

        val formattedAmount = CurrencyUtils.assertCurrency(currencyCode)

        assertEquals(Unit, formattedAmount)
    }

    @Test
    fun `assert currency throws exception, if currency code is not supported`() {
        val currencyCode = "AAA"

        val thrown = assertThrows(CheckoutException::class.java) { CurrencyUtils.assertCurrency(currencyCode) }

        assertEquals("Currency $currencyCode not supported", thrown.message)
    }
}
