/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/1/2023.
 */

package com.adyen.checkout.card.internal.util

internal object CardNumberUtils {

    fun formatCardNumber(unformattedString: String, maskPartsLengths: List<Int>, separator: String): String {
        val separatorIndexes = List(maskPartsLengths.size) { index -> maskPartsLengths.take(index + 1).sum() }
        return unformattedString.foldIndexed("") { index, accumulation, char ->
            if (separatorIndexes.contains(index)) "$accumulation$separator$char" else accumulation + char
        }
    }
}
