/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/8/2022.
 */

package com.adyen.checkout.card.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.Environment

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DualBrandedCardHandler(private val environment: Environment) {

    internal fun processDetectedCardTypes(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?
    ): DualBrandData? {
        if (!isDualBrandedFlow(detectedCardTypes)) return null
        return DualBrandData(
            selectedBrand = getSelectedCardType(detectedCardTypes, selectedBrand)?.cardBrand,
            brandOptions = mapToCardBrandItemList(detectedCardTypes, selectedBrand),
        )
    }

    private fun getSelectedCardType(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?
    ): DetectedCardType? {
        return detectedCardTypes.firstOrNull { it.cardBrand.txVariant == selectedBrand?.txVariant }
    }

    private fun isDualBrandedFlow(detectedCardTypes: List<DetectedCardType>): Boolean {
        return detectedCardTypes.filter { it.isSupported && it.isReliable }.size > 1
    }

    private fun mapToCardBrandItemList(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?
    ): List<CardBrandItem> {
        val filteredCardBrands = detectedCardTypes.filter { it.isSupported && it.isReliable }
        return filteredCardBrands.mapIndexed { index, detectedCardType ->
            if (selectedBrand == null) {
                detectedCardType.mapToCardBrandItem(index == 0)
            } else {
                detectedCardType.mapToCardBrandItem(
                    detectedCardType.cardBrand.txVariant == selectedBrand.txVariant,
                )
            }
        }
    }

    private fun DetectedCardType.mapToCardBrandItem(isSelected: Boolean) = CardBrandItem(
        name = localizedBrand ?: cardBrand.txVariant,
        brand = cardBrand,
        isSelected = isSelected,
        environment = environment,
    )
}
