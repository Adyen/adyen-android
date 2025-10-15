/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import com.adyen.checkout.card.old.internal.util.CardNumberUtils

internal class CardNumberOutputTransformation(
    val isAmex: Boolean,
) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val outText = CardNumberUtils.formatCardNumber(
            unformattedString = toString(),
            maskPartsLengths = if (isAmex) {
                AMEX_CARD_NUMBER_MASK
            } else {
                DEFAULT_CARD_NUMBER_MASK
            },
            separator = DIGIT_SEPARATOR,
        )
        replace(0, length, outText)
    }

    companion object {
        private const val DIGIT_SEPARATOR = " "

        // 4 characters then 6 then 5 then 4. Example: 1234 123456 12345 1234
        private val AMEX_CARD_NUMBER_MASK = listOf(4, 6, 5, 4)

        // 4 characters then 4 then 4 then 4 then 3. Example: 1234 1234 1234 1234 123
        private val DEFAULT_CARD_NUMBER_MASK = listOf(4, 4, 4, 4, 3)
    }
}
