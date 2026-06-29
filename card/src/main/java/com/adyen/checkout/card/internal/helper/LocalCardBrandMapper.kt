/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/6/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType

internal object LocalCardBrandMapper {

    private val NO_CVC_BRANDS: Set<CardBrand> = setOf(CardBrand(txVariant = CardType.BCMC.txVariant))

    fun map(
        cardBrand: CardBrand,
        isSupported: Boolean,
        hideCvc: Boolean,
    ): DetectedCardType {
        return DetectedCardType(
            cardBrand = cardBrand,
            enableLuhnCheck = true,
            cvcPolicy = when {
                hideCvc || NO_CVC_BRANDS.contains(cardBrand) -> Brand.FieldPolicy.HIDDEN
                else -> Brand.FieldPolicy.REQUIRED
            },
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = isSupported,
            isHidden = false,
            isShopperSelectionAllowedInDualBranded = false,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }
}
