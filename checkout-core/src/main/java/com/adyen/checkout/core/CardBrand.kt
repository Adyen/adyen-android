/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/10/2024.
 */
package com.adyen.checkout.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class represents a card scheme. The constructor allows for creating a [CardBrand] with a scheme that is not in
 * the predefined list of [CardType]. Can be used to configure the supported card schemes with
 * [CardConfiguration.Builder.setSupportedCardTypes].
 */
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
