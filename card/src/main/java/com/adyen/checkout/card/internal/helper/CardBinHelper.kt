/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/7/2026.
 */

package com.adyen.checkout.card.internal.helper

internal object CardBinHelper {

    /**
     * Returns the first 8 digits of the card number if the card number is valid, or up to the first 6 digits if the
     * card number is not valid.
     *
     * This is the actual BIN returned to merchants in the public API and is different from the one used internally for
     * card brand detection (BIN lookup).
     */
    fun getBin(cardNumber: String, isValid: Boolean): String {
        return if (isValid && cardNumber.length >= EXTENDED_CARD_NUMBER_LENGTH) {
            cardNumber.take(BIN_VALUE_EXTENDED_LENGTH)
        } else {
            cardNumber.take(BIN_VALUE_LENGTH)
        }
    }

    private const val BIN_VALUE_LENGTH = 6
    private const val BIN_VALUE_EXTENDED_LENGTH = 8
    private const val EXTENDED_CARD_NUMBER_LENGTH = 16
}
