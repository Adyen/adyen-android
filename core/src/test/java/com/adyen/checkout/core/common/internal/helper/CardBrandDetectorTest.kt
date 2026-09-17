/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/9/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.common.CardBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class CardBrandDetectorTest {

    @Test
    fun `when estimate is called with a complete card number, then the matching brand is estimated`() {
        val estimatedBrands = CardBrandDetector.estimate(AMEX_CARD_NUMBER)

        assertEquals(listOf(CardBrand(txVariant = "amex")), estimatedBrands)
    }

    @Test
    fun `when estimate is called with a partial card number, then every brand it can still become is estimated`() {
        val estimatedBrands = CardBrandDetector.estimate("4571")

        assertEquals(
            listOf(
                CardBrand(txVariant = "cartebancaire"),
                CardBrand(txVariant = "visa"),
                CardBrand(txVariant = "visadankort"),
            ),
            estimatedBrands,
        )
    }

    @Test
    fun `when estimate is called with a card number containing whitespace, then the whitespace is ignored`() {
        val estimatedBrands = CardBrandDetector.estimate("  3700 0000 0000 002 ")

        assertEquals(CardBrandDetector.estimate(AMEX_CARD_NUMBER), estimatedBrands)
    }

    @Test
    fun `when estimate is called with a card number of an unknown brand, then that brand is not estimated`() {
        val estimatedBrands = CardBrandDetector.estimate(SODEXO_CARD_NUMBER)

        assertFalse(estimatedBrands.contains(CardBrand(txVariant = "sodexo")))
    }

    companion object {
        private const val AMEX_CARD_NUMBER = "370000000000002"
        private const val SODEXO_CARD_NUMBER = "6033890257034050"
    }
}
