/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import androidx.compose.ui.text.input.KeyboardType
import com.adyen.checkout.card.internal.ui.view.SocialSecurityNumberProperties.ALL_SEPARATORS
import com.adyen.checkout.card.internal.ui.view.SocialSecurityNumberProperties.MAX_LENGTH_UNFORMATTED

internal class SocialSecurityNumberInputTransformation : InputTransformation {

    override val keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    override fun TextFieldBuffer.transformInput() {
        val text = asCharSequence()

        // only allow digits and separators
        if (text.any { !it.isDigit() && it !in ALL_SEPARATORS }) {
            revertAllChanges()
            return
        }

        // enforce max length (without separators)
        val digitsOnlyText = text.filter { it.isDigit() }
        if (digitsOnlyText.length > MAX_LENGTH_UNFORMATTED) {
            revertAllChanges()
            return
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
