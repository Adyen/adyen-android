/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/4/2026.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
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
        return matchingCardBrands.map(::mapCardBrands)
    }

    private fun mapCardBrands(cardBrand: CardBrand): DetectedCardType {
        return DetectedCardType(
            cardBrand = cardBrand,
            isReliable = false,
            enableLuhnCheck = true,
            cvcPolicy = when {
                NO_CVC_BRANDS.contains(cardBrand) -> Brand.FieldPolicy.HIDDEN
                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = supportedCardBrands.contains(cardBrand),
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }

    companion object {
        private val NO_CVC_BRANDS: Set<CardBrand> = hashSetOf(
            CardBrand(txVariant = CardType.BCMC.txVariant),
        )
    }
}
