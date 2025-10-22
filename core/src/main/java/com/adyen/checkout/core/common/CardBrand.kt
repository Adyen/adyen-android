/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */
package com.adyen.checkout.core.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class represents a card scheme. The constructor allows for creating a [CardBrand] with a scheme that is not in
 * the predefined list of [CardType]. Can be used to configure the supported card schemes with
 * [CardConfigurationBuilder.supportedCardTypes].
 */
@Parcelize
data class CardBrand(val txVariant: String) : Parcelable {
    companion object {
        /**
         * Estimate all potential [CardBrands][CardBrand] for a given card number.
         *
         * @param cardNumber The potential card number.
         * @return All matching [CardBrands][CardBrand] if the number was valid, otherwise an empty [List].
         */
        fun estimate(cardNumber: String): List<CardBrand> {
            return CardType.entries.filter { it.isEstimateFor(cardNumber) }.map { CardBrand(txVariant = it.txVariant) }
        }
    }
}
