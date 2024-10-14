/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType

internal object DualBrandedCardUtils {

    fun sortBrands(cards: List<DetectedCardType>): List<DetectedCardType> {
        return if (cards.size <= 1) {
            cards
        } else {
            val hasCarteBancaire = cards.any { it.cardBrand == CardBrand(cardType = CardType.CARTEBANCAIRE) }
            val hasVisa = cards.any { it.cardBrand == CardBrand(cardType = CardType.VISA) }
            val hasPlcc = cards.any {
                it.cardBrand.txVariant.contains("plcc") ||
                    it.cardBrand.txVariant.contains("cbcc")
            }

            when {
                hasCarteBancaire && hasVisa -> cards.sortedByDescending {
                    it.cardBrand == CardBrand(cardType = CardType.VISA)
                }

                hasPlcc -> cards.sortedByDescending {
                    it.cardBrand.txVariant.contains("plcc") ||
                        it.cardBrand.txVariant.contains("cbcc")
                }

                else -> cards
            }
        }
    }
}
