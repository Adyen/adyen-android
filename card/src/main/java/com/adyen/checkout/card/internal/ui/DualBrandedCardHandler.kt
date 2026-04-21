/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.card.internal.ui.state.CardBrandData
import com.adyen.checkout.card.internal.ui.state.CardBrandState
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType

internal class DualBrandedCardHandler {

    internal fun getDualBrandData(
        cardBrandState: CardBrandState,
        selectedBrand: CardBrand?,
    ): DualBrandData? {
        if (cardBrandState !is CardBrandState.DualBrand || !isShopperSelectionAllowed(cardBrandState)) return null

        val brandOptions = mapToCardBrandItemList(
            cardBrandDataList = cardBrandState.cardBrandDataList,
            selectedBrand = selectedBrand,
        )

        return DualBrandData(
            brandOptionFirst = brandOptions[0],
            brandOptionSecond = brandOptions[1],
        )
    }

    private fun isShopperSelectionAllowed(cardBrandState: CardBrandState.DualBrand): Boolean {
        return cardBrandState.cardBrandDataList.any { it.cardBrand.txVariant in SUPPORTED_CARD_BRANDS }
    }

    private fun mapToCardBrandItemList(
        cardBrandDataList: List<CardBrandData>,
        selectedBrand: CardBrand?,
    ): List<CardBrandItem> {
        return cardBrandDataList.mapIndexed { index, cardBrandData ->
            if (selectedBrand == null) {
                cardBrandData.mapToCardBrandItem(index == 0)
            } else {
                cardBrandData.mapToCardBrandItem(
                    cardBrandData.cardBrand.txVariant == selectedBrand.txVariant,
                )
            }
        }
    }

    private fun CardBrandData.mapToCardBrandItem(isSelected: Boolean) = CardBrandItem(
        name = localizedBrand ?: cardBrand.txVariant,
        brand = cardBrand,
        isSelected = isSelected,
    )

    companion object {
        private val SUPPORTED_CARD_BRANDS = listOf(
            CardType.CARTEBANCAIRE,
            CardType.BCMC,
            CardType.DANKORT,
        ).map { it.txVariant }
    }
}
