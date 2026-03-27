/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete

/**
 * Handles the logic of [DigitOnlyInputTransformation] which allows custom InputTransformation classes to reuse this
 * logic and extend it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DigitOnlyTextFieldBufferTransformation(
    private val allowedSeparators: List<Char> = emptyList(),
    private val maxLengthWithoutSeparators: Int? = null,
) {
    /**
     * Returns whether the changes were accepted (true) or rejected (false)
     */
    fun transformInput(textFieldBuffer: TextFieldBuffer): Boolean = with(textFieldBuffer) {
        val text = asCharSequence()

        // only allow digits and separators
        if (text.any { !it.isDigit() && it !in allowedSeparators }) {
            revertAllChanges()
            return false
        }

        if (maxLengthWithoutSeparators != null) {
            // enforce max length
            val digitsOnlyText = text.filter { it.isDigit() }
            if (digitsOnlyText.length > maxLengthWithoutSeparators) {
                revertAllChanges()
                return false
            }
        }

        // set the raw value as digits only so that the OutputTransformation can format cleanly
        // we surgically remove non-digit characters one by one to preserve cursor position
        for (charIndex in length - 1 downTo 0) {
            if (!text[charIndex].isDigit()) {
                delete(charIndex, charIndex + 1)
            }
        }

        return true
    }
}
