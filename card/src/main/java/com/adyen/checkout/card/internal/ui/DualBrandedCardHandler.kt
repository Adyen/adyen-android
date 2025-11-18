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

        val isSelectable = isSelectable(reliableAndSupportedTypes = reliableAndSupportedTypes)
        val brandOptions = mapToCardBrandItemList(
            detectedCardTypes = detectedCardTypes,
            selectedBrand = selectedBrand,
            isSelectable = isSelectable,
        )

        val selectedCardBrand = getSelectedCardBrand(
            isSelectable = isSelectable,
            detectedCardTypes = detectedCardTypes,
            selectedBrand = selectedBrand,
            brandOptions = brandOptions,
        )

        return DualBrandData(
            selectedBrand = selectedCardBrand,
            brandOptions = brandOptions,
            selectable = isSelectable,
        )
    }

    private fun isSelectable(reliableAndSupportedTypes: List<DetectedCardType>) = reliableAndSupportedTypes.any {
        it.cardBrand.txVariant in SUPPORTED_CARD_BRANDS
    }

    private fun getSelectedCardBrand(
        isSelectable: Boolean,
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?,
        brandOptions: List<CardBrandItem>
    ): CardBrand? {
        return if (isSelectable) {
            findSelectedCardType(detectedCardTypes, selectedBrand)?.cardBrand
                ?: brandOptions.firstOrNull()?.brand
        } else {
            null
        }
    }

    private fun findSelectedCardType(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?
    ): DetectedCardType? {
        return detectedCardTypes.find { it.cardBrand.txVariant == selectedBrand?.txVariant }
    }

    private fun isDualBrandedFlow(reliableAndSupportedTypes: List<DetectedCardType>) =
        reliableAndSupportedTypes.size > 1

    private fun mapToCardBrandItemList(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?,
        isSelectable: Boolean,
    ): List<CardBrandItem> {
        val filteredCardBrands = detectedCardTypes.filter { it.isSupported && it.isReliable }
        return filteredCardBrands.mapIndexed { index, detectedCardType ->
            if (selectedBrand == null) {
                detectedCardType.mapToCardBrandItem(isSelectable && index == 0)
            } else {
                detectedCardType.mapToCardBrandItem(
                    isSelectable && detectedCardType.cardBrand.txVariant == selectedBrand.txVariant,
                )
            }
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
