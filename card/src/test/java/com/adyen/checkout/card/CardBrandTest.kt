package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.CardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardBrandTest {

    @Test
    fun `test if card number is not part of predefined card brand enum`() {
        val sodexoCard = CardBrand(txVariant = "sodexo")
        val cardTypes = CardType.estimate(SODEXO_CARD_NUMBER)

        val isPredefinedBrand = cardTypes.contains(sodexoCard.cardType)

        assertFalse(isPredefinedBrand)
    }

    @Test
    fun `test if card number is part of predefined card brand enum`() {
        val amexCard = CardBrand(CardType.AMERICAN_EXPRESS)
        val cardTypes = CardType.estimate(AMEX_CARD_NUMBER)

        val isPredefinedBrand = cardTypes.contains(amexCard.cardType)

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

        assertEquals(result, CardType.AMERICAN_EXPRESS)
    }

    companion object {
        private const val SODEXO_CARD_NUMBER = "6033890257034050"
        private const val AMEX_CARD_NUMBER = "370000000000002"
    }
}
