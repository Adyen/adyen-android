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
import androidx.compose.ui.text.input.KeyboardType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DigitOnlyInputTransformation(
    allowedSeparators: List<Char> = emptyList(),
    maxLengthWithoutSeparators: Int? = null,
) : InputTransformation {

    private val digitOnlyTextFieldBufferTransformation = DigitOnlyTextFieldBufferTransformation(
        allowedSeparators,
        maxLengthWithoutSeparators,
    )

    override val keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    override fun TextFieldBuffer.transformInput() {
        digitOnlyTextFieldBufferTransformation.transformInput(this)
    }
}
