/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CardNumberFormatTest {

    @Test
    fun `when card brand is amex, then format is AMEX`() {
        // GIVEN
        val brand = CardBrand(CardType.AMERICAN_EXPRESS.txVariant)

        // WHEN
        val result = brand.toCardNumberFormat()

        // THEN
        assertEquals(CardNumberFormat.AMEX, result)
    }

    @Test
    fun `when card brand is visa, then format is DEFAULT`() {
        // GIVEN
        val brand = CardBrand("visa")

        // WHEN
        val result = brand.toCardNumberFormat()

        // THEN
        assertEquals(CardNumberFormat.DEFAULT, result)
    }

    @Test
    fun `when card brand is mastercard, then format is DEFAULT`() {
        // GIVEN
        val brand = CardBrand("mc")

        // WHEN
        val result = brand.toCardNumberFormat()

        // THEN
        assertEquals(CardNumberFormat.DEFAULT, result)
    }

    @Test
    fun `when card brand is null, then format is DEFAULT`() {
        // GIVEN
        val brand: CardBrand? = null

        // WHEN
        val result = brand.toCardNumberFormat()

        // THEN
        assertEquals(CardNumberFormat.DEFAULT, result)
    }
}
