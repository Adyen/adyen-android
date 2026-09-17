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
 * This class represents a card scheme. Use one of the predefined brands, or the constructor with a `txVariant` to
 * create a [CardBrand] with a scheme that is not predefined. Can be used to configure the supported card schemes with
 * `CardConfiguration.supportedCardBrands`.
 */
@Parcelize
data class CardBrand(val txVariant: String) : Parcelable {

    companion object {

        val AMERICAN_EXPRESS = CardBrand("amex")
        val ARGENCARD = CardBrand("argencard")
        val BCMC = CardBrand("bcmc")
        val BIJENKORF_CARD = CardBrand("bijcard")
        val CABAL = CardBrand("cabal")
        val CARTEBANCAIRE = CardBrand("cartebancaire")
        val CODENSA = CardBrand("codensa")
        val CUP = CardBrand("cup")
        val DANKORT = CardBrand("dankort")
        val DINERS = CardBrand("diners")
        val DISCOVER = CardBrand("discover")
        val ELO = CardBrand("elo")
        val FORBRUGSFORENINGEN = CardBrand("forbrugsforeningen")
        val HIPER = CardBrand("hiper")
        val HIPERCARD = CardBrand("hipercard")
        val JCB = CardBrand("jcb")
        val KARENMILLER = CardBrand("karenmillen")
        val LASER = CardBrand("laser")
        val MAESTRO = CardBrand("maestro")
        val MAESTRO_UK = CardBrand("maestrouk")
        val MASTERCARD = CardBrand("mc")
        val MCALPHABANKBONUS = CardBrand("mcalphabankbonus")
        val MIR = CardBrand("mir")
        val NARANJA = CardBrand("naranja")
        val OASIS = CardBrand("oasis")
        val SHOPPING = CardBrand("shopping")
        val SOLO = CardBrand("solo")
        val TROY = CardBrand("troy")
        val UATP = CardBrand("uatp")
        val VISA = CardBrand("visa")
        val VISAALPHABANKBONUS = CardBrand("visaalphabankbonus")
        val VISADANKORT = CardBrand("visadankort")
        val WAREHOUSE = CardBrand("warehouse")
    }
}
