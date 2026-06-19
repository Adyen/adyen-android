/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/4/2026.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.helper.LocalCardBrandMapper
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.helper.adyenLog

internal class LocalCardBrandDetectionService(
    private val supportedCardBrands: List<CardBrand>,
) {

    fun getCardBrands(cardNumber: String): List<DetectedCardType> {
        adyenLog(AdyenLogLevel.DEBUG) { "getting local card brands" }
        if (cardNumber.isEmpty()) {
            return emptyList()
        }
        val matchingCardBrands = CardBrand.estimate(cardNumber)
        return matchingCardBrands.map {
            LocalCardBrandMapper.map(
                cardBrand = it,
                isSupported = supportedCardBrands.contains(it),
                hideCvc = false,
            )
        }
    }
}
