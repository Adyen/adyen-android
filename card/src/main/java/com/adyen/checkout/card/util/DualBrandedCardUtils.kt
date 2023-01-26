package com.adyen.checkout.card.util

import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType

object DualBrandedCardUtils {

    fun sortBrands(cards: List<DetectedCardType>): List<DetectedCardType> {
        return if (cards.size <= 1) {
            cards
        } else {
            val hasCarteBancaire = cards.any { it.cardType == CardType(cardBrand = CardBrand.CARTEBANCAIRE) }
            val hasVisa = cards.any { it.cardType == CardType(cardBrand = CardBrand.VISA) }
            val hasPlcc = cards.any {
                it.cardType.txVariant.contains("plcc") ||
                    it.cardType.txVariant.contains("cbcc")
            }

            when {
                hasCarteBancaire && hasVisa -> cards.sortedByDescending {
                    it.cardType == CardType(cardBrand = CardBrand.VISA)
                }
                hasPlcc -> cards.sortedByDescending {
                    it.cardType.txVariant.contains("plcc") ||
                        it.cardType.txVariant.contains("cbcc")
                }
                else -> cards
            }
        }
    }
}
