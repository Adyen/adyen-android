/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/9/2019.
 */
package com.adyen.checkout.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardBrand constructor(val txVariant: String) : Parcelable {

    /**
     * Use this constructor when defining the supported card brand predefined inside [CardType] enum
     * inside your component
     */
    constructor(cardType: CardType) : this(txVariant = cardType.txVariant)

    companion object {
        /**
         * Estimate all potential [CardBrands][CardBrand] for a given card number.
         *
         * @param cardNumber The potential card number.
         * @return All matching [CardBrands][CardBrand] if the number was valid, otherwise an empty [List].
         */
        fun estimate(cardNumber: String): List<CardBrand> {
            return CardType.values().filter { it.isEstimateFor(cardNumber) }.map { CardBrand(cardType = it) }
        }
    }
}
