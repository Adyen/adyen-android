/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/8/2025.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import androidx.compose.ui.text.input.KeyboardType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DigitOnlyInputTransformation(
    private val allowedSeparators: List<Char> = emptyList(),
    private val maxLengthWithoutSeparators: Int? = null
) : InputTransformation {

    override val keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    override fun TextFieldBuffer.transformInput() {
        val text = asCharSequence()

        // only allow digits and separators
        if (text.any { !it.isDigit() && it !in allowedSeparators }) {
            revertAllChanges()
            return
        }

        if (maxLengthWithoutSeparators != null) {
            // enforce max length
            val digitsOnlyText = text.filter { it.isDigit() }
            if (digitsOnlyText.length > maxLengthWithoutSeparators) {
                revertAllChanges()
                return
            }
        }

        // set the raw value as digits only so that the OutputTransformation can format cleanly
        // we surgically remove non-digit characters one by one to preserve cursor position
        for (charIndex in length - 1 downTo 0) {
            if (!text[charIndex].isDigit()) {
                delete(charIndex, charIndex + 1)
            }
        }
    }
}
