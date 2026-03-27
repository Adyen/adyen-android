/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/10/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.core.common.internal.properties.ExpiryDateProperties.EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS
import com.adyen.checkout.core.common.internal.properties.ExpiryDateProperties.EXPIRY_DATE_SEPARATOR
import com.adyen.checkout.ui.internal.element.input.DigitOnlyTextFieldBufferTransformation

internal class ExpiryDateInputTransformation : InputTransformation {

    override val keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    private val digitOnlyTextFieldBufferTransformation = DigitOnlyTextFieldBufferTransformation(
        allowedSeparators = listOf(EXPIRY_DATE_SEPARATOR),
        maxLengthWithoutSeparators = EXPIRY_DATE_MAX_LENGTH_NO_SEPARATORS,
    )

    override fun TextFieldBuffer.transformInput() {
        val areChangesAccepted = digitOnlyTextFieldBufferTransformation.transformInput(this)
        if (!areChangesAccepted) return

        val text = asCharSequence()

        // If input is one digit larger than 1, automatically insert 0 prefix to correctly format the month
        val shouldAddZeroPrefix = text.length == 1 && text[0].digitToInt() > MAX_FIRST_MONTH_DIGIT_VALUE
        if (shouldAddZeroPrefix) {
            insert(0, PREFIX_ZERO)
        }
    }

    companion object {
        private const val MAX_FIRST_MONTH_DIGIT_VALUE = 1
        private const val PREFIX_ZERO = "0"
    }
}
