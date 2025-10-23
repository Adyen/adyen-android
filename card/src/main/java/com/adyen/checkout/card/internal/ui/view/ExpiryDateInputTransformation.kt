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
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly

internal class ExpiryDateInputTransformation : InputTransformation {

    override val keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    override fun TextFieldBuffer.transformInput() {
        val input = asCharSequence()
        val isInputOnlyDigits = input.filter { it != SEPARATOR }.isDigitsOnly()
        val hasSeparator = input.filter { it == SEPARATOR }.length > MAX_SEPARATOR_COUNT
        if (!isInputOnlyDigits || hasSeparator) {
            revertAllChanges()
        }
        // If first month digit is larger than 1, automatically insert 0 prefix
        val shouldAddZeroPrefix = input.length == 1 && input[0].digitToInt() > MAX_FIRST_MONTH_DIGIT_VALUE
        if (shouldAddZeroPrefix) {
            insert(0, PREFIX_ZERO)
        }
        // If input is 123 after last key stroke, insert separator after month digits
        val shouldAddSeparator = input.length == MONTH_LENGTH + 1 && input.last() != SEPARATOR
        if (shouldAddSeparator) {
            insert(2, SEPARATOR.toString())
        }
        // If input is 12/ after digit deletion or manually inserting separator
        val shouldRemoveSeparator = input.length == MONTH_LENGTH + 1 && input.last() == SEPARATOR
        if (shouldRemoveSeparator) {
            delete(MONTH_LENGTH, input.length)
        }
    }

    companion object {
        // Including separator
        const val MAX_DIGITS = 5
        private const val MAX_SEPARATOR_COUNT = 1
        private const val MONTH_LENGTH = 2
        private const val SEPARATOR = '/'
        private const val MAX_FIRST_MONTH_DIGIT_VALUE = 1
        private const val PREFIX_ZERO = "0"
    }
}
