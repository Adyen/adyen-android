/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/4/2026.
 */

package com.adyen.checkout.card.internal.helper

internal class DetectCardTypeBinHelper {

    /**
     * Returns the BIN if the card number is equal or longer than the BIN length.
     *
     * This BIN is only used internally for card brand detection and is different from the actual BIN which is returned
     * in the public API.
     */
    fun getCardDetectionBin(cardNumber: String): String? {
        return cardNumber.takeIf { it.length >= BIN_LENGTH }?.take(BIN_LENGTH)
    }

    companion object {
        private const val BIN_LENGTH = 11
    }
}
