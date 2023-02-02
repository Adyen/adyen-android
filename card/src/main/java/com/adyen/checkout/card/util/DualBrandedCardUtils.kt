package com.adyen.checkout.card.util

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType

object DualBrandedCardUtils {

    fun sortBrands(cards: List<DetectedCardType>): List<DetectedCardType> {
        return if (cards.size <= 1) {
            cards
        } else {
            val hasCarteBancaire = cards.any { it.cardBrand.cardType == CardType.CARTEBANCAIRE }
            val hasVisa = cards.any { it.cardBrand.cardType == CardType.VISA }
            val hasPlcc = cards.any {
                it.cardBrand.txVariant.contains("plcc") ||
                    it.cardBrand.txVariant.contains("cbcc")
            }

            when {
                hasCarteBancaire && hasVisa -> cards.sortedByDescending { it.cardBrand.cardType == CardType.VISA }
                hasPlcc -> cards.sortedByDescending {
                    it.cardBrand.txVariant.contains("plcc") ||
                        it.cardBrand.txVariant.contains("cbcc")
                }
                else -> cards
            }
        }
    }
}
