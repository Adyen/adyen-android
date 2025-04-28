/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/8/2022.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardBrandItem

internal object DetectedCardTypesUtils {

    fun getSelectedOrFirstDetectedCardType(
        detectedCardTypes: List<DetectedCardType>,
        selectedCardBrandItem: CardBrandItem?
    ): DetectedCardType? {
        val selectedCardType = getSelectedCardType(detectedCardTypes, selectedCardBrandItem)
        return selectedCardType ?: detectedCardTypes.firstOrNull()
    }

    fun getSelectedCardType(
        detectedCardTypes: List<DetectedCardType>,
        selectedCardBrandItem: CardBrandItem?
    ): DetectedCardType? {
        return detectedCardTypes.firstOrNull { it.cardBrand.txVariant == selectedCardBrandItem?.brand?.txVariant }
    }
}
