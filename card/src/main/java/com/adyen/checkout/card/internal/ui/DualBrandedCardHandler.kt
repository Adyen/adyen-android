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

internal class DualBrandedCardHandler {

    internal fun getDualBrandData(cardBrandState: CardBrandState): DualBrandData? {
        if (cardBrandState !is CardBrandState.DualBrandWithShopperSelection) return null

        val brandOptions = mapToCardBrandItemList(
            cardBrandDataList = cardBrandState.cardBrandDataList,
            selectedCardBrandData = cardBrandState.shopperSelectedCardBrandData,
        )

        return DualBrandData(
            brandOptionFirst = brandOptions[0],
            brandOptionSecond = brandOptions[1],
        )
    }

    private fun mapToCardBrandItemList(
        cardBrandDataList: List<CardBrandData>,
        selectedCardBrandData: CardBrandData,
    ): List<CardBrandItem> {
        return cardBrandDataList.map { cardBrandData ->
            cardBrandData.mapToCardBrandItem(
                isSelected = (cardBrandData == selectedCardBrandData),
            )
        }
    }

    private fun CardBrandData.mapToCardBrandItem(isSelected: Boolean) = CardBrandItem(
        name = localizedBrand ?: cardBrand.txVariant,
        brand = cardBrand,
        isSelected = isSelected,
    )
}
