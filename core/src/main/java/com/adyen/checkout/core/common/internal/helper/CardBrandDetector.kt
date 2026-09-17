/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/9/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.CardBrand
import java.util.regex.Pattern

/**
 * Detects the [CardBrands][CardBrand] of a card number, based on the regexes of the predefined brands.
 *
 * [Pattern] is used instead of Kotlin's [Regex] because estimation relies on [java.util.regex.Matcher.hitEnd] to also
 * match a card number that is still being typed.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardBrandDetector {

    private val WHITESPACE_REGEX = "\\s".toRegex()

    /**
     * The declaration order is significant: [estimate] returns brands in this order, which acts as a priority when
     * multiple brands match. Reordering this map changes which brand is preselected for dual branded cards.
     */
    private val REGEXES: Map<CardBrand, Pattern> = mapOf(
        CardBrand.AMERICAN_EXPRESS to Pattern.compile("^3[47][0-9]{0,13}$"),
        CardBrand.ARGENCARD to Pattern.compile("^(50)(1)\\d*$"),
        CardBrand.BCMC to Pattern.compile("^((6703)[0-9]{0,15}|(479658|606005)[0-9]{0,13})$"),
        CardBrand.BIJENKORF_CARD to Pattern.compile("^(5100081)[0-9]{0,9}$"),
        CardBrand.CABAL to Pattern.compile("^(58|6[03])([03469])\\d*$"),
        CardBrand.CARTEBANCAIRE to Pattern.compile("^[4-6][0-9]{0,15}$"),
        CardBrand.CODENSA to Pattern.compile("^(590712)[0-9]{0,10}$"),
        CardBrand.CUP to Pattern.compile("^(62|81)[0-9]{0,17}$"),
        CardBrand.DANKORT to Pattern.compile("^(5019)[0-9]{0,12}$"),
        CardBrand.DINERS to Pattern.compile("^(36)[0-9]{0,12}$"),
        CardBrand.DISCOVER to Pattern.compile(
            "^(6011[0-9]{0,12}|(644|645|646|647|648|649)[0-9]{0,13}|65[0-9]{0,14})$",
        ),
        CardBrand.ELO to Pattern.compile(
            "^((((506699)|(506770)|(506771)|(506772)|(506773)|(506774)|(506775)|(506776)|(506777)|(506778)|" +
                "(401178)|(438935)|(451416)|(457631)|(457632)|(504175)|(627780)|(636368)|(636297))[0-9]{0,10})|" +
                "((50676)|(50675)|(50674)|(50673)|(50672)|(50671)|(50670))[0-9]{0," + "11})$",
        ),
        CardBrand.FORBRUGSFORENINGEN to Pattern.compile("^(60)(0)\\d*$"),
        CardBrand.VISAALPHABANKBONUS to Pattern.compile("^(450903)[0-9]{0,10}$"),
        CardBrand.MCALPHABANKBONUS to Pattern.compile("^(510099)[0-9]{0,10}$"),
        CardBrand.HIPER to Pattern.compile("^(637095|637599|637609|637612)[0-9]{0,10}$"),
        CardBrand.HIPERCARD to Pattern.compile("^(606282)[0-9]{0,10}$"),
        CardBrand.JCB to Pattern.compile("^(352[8,9]{1}[0-9]{0,15}|35[4-8]{1}[0-9]{0,16})$"),
        CardBrand.OASIS to Pattern.compile("^(982616)[0-9]{0,10}$"),
        CardBrand.KARENMILLER to Pattern.compile("^(98261465)[0-9]{0,8}$"),
        CardBrand.WAREHOUSE to Pattern.compile("^(982633)[0-9]{0,10}$"),
        CardBrand.LASER to Pattern.compile("^(6304|6706|6709|6771)[0-9]{0,15}$"),
        CardBrand.MAESTRO to Pattern.compile("^(5[0|6-8][0-9]{0,17}|6[0-9]{0,18})$"),
        CardBrand.MAESTRO_UK to Pattern.compile("^(6759)[0-9]{0,15}$"),
        CardBrand.MASTERCARD to Pattern.compile("^(5[1-5][0-9]{0,14}|2[2-7][0-9]{0,14})$"),
        CardBrand.MIR to Pattern.compile("^(220)[0-9]{0,16}$"),
        CardBrand.NARANJA to Pattern.compile("^(37|40|5[28])([279])\\d*$"),
        CardBrand.SHOPPING to Pattern.compile("^(27|58|60)([39])\\d*$"),
        CardBrand.SOLO to Pattern.compile("^(6767)[0-9]{0,15}$"),
        CardBrand.TROY to Pattern.compile("^(97)(9)\\d*$"),
        CardBrand.UATP to Pattern.compile("^1[0-9]{0,14}$"),
        CardBrand.VISA to Pattern.compile("^4[0-9]{0,18}$"),
        CardBrand.VISADANKORT to Pattern.compile("^(4571)[0-9]{0,12}$"),
    )

    /**
     * Estimate all potential [CardBrands][CardBrand] for a given card number.
     *
     * @param cardNumber The potential card number.
     * @return All matching [CardBrands][CardBrand] if the number was valid, otherwise an empty [List].
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun estimate(cardNumber: String): List<CardBrand> {
        val normalizedCardNumber = cardNumber.replace(WHITESPACE_REGEX, "")
        return REGEXES.filterValues { it.isEstimateFor(normalizedCardNumber) }.keys.toList()
    }

    private fun Pattern.isEstimateFor(cardNumber: String): Boolean {
        val matcher = matcher(cardNumber)
        return matcher.matches() || matcher.hitEnd()
    }
}
