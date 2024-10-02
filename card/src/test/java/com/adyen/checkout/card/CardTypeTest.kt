/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 25/1/2023.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CardTypeTest {

    @Test
    fun `test if card number is not part of predefined card brand enum`() {
        val sodexoCard = CardBrand(txVariant = "sodexo")
        val cardTypes = CardBrand.estimate(SODEXO_CARD_NUMBER)

        val isPredefinedBrand = cardTypes.contains(sodexoCard)

        assertFalse(isPredefinedBrand)
    }

    @Test
    fun `test if card number is part of predefined card brand enum`() {
        val amexCard = CardBrand(cardType = CardType.AMERICAN_EXPRESS)
        val cardTypes = CardBrand.estimate(AMEX_CARD_NUMBER)

        val isPredefinedBrand = cardTypes.contains(amexCard)

        assertTrue(isPredefinedBrand)
    }

    @Test
    fun `test if card brand is not part of predefined card brand enum`() {
        val sodexoCard = CardBrand(txVariant = "sodexo")

        val result = CardType.getByBrandName(sodexoCard.txVariant)

        assertNull(result)
    }

    @Test
    fun `test if card brand is part of predefined card brand enum`() {
        val amexCard = CardBrand(txVariant = "amex")

        val result = CardType.getByBrandName(amexCard.txVariant)

        assertNotNull(result)
    }

    companion object {
        private const val SODEXO_CARD_NUMBER = "6033890257034050"
        private const val AMEX_CARD_NUMBER = "370000000000002"
    }
}
