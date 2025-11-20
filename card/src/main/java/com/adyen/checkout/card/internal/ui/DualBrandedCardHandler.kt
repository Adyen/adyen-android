/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType

internal class DualBrandedCardHandler {

    internal fun processDetectedCardTypes(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?
    ): DualBrandData? {
        val reliableAndSupportedTypes = detectedCardTypes.filter { it.isSupported && it.isReliable }

        if (!isDualBrandedFlow(reliableAndSupportedTypes)) return null

        val brandOptions = mapToCardBrandItemList(
            reliableAndSupportedTypes = reliableAndSupportedTypes,
            selectedBrand = selectedBrand,
        )

        return DualBrandData(
            brandOptionFirst = brandOptions[0],
            brandOptionSecond = brandOptions[1],
        )
    }

    private fun isDualBrandedFlow(reliableAndSupportedTypes: List<DetectedCardType>) =
        reliableAndSupportedTypes.size > 1 && hasSupportedBrands(reliableAndSupportedTypes)

    private fun hasSupportedBrands(reliableAndSupportedTypes: List<DetectedCardType>) = reliableAndSupportedTypes.any {
        it.cardBrand.txVariant in SUPPORTED_CARD_BRANDS
    }

    private fun mapToCardBrandItemList(
        reliableAndSupportedTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?,
    ) = reliableAndSupportedTypes.mapIndexed { index, detectedCardType ->
        if (selectedBrand == null) {
            detectedCardType.mapToCardBrandItem(index == 0)
        } else {
            detectedCardType.mapToCardBrandItem(
                detectedCardType.cardBrand.txVariant == selectedBrand.txVariant,
            )
        }
    }

    private fun DetectedCardType.mapToCardBrandItem(isSelected: Boolean) = CardBrandItem(
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
