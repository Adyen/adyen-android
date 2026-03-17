/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2026.
 */

package com.adyen.checkout.ui.internal.element.input

import androidx.annotation.RestrictTo
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/**
 * Allows formatting text fields based on a list of separators and indexes. To extend this logic you can use
 * [SeparatorsTextFieldBufferTransformation] (for example if there are multiple possible list of separators).
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SeparatorsOutputTransformation(private val separators: List<TextFieldSeparator>) : OutputTransformation {

    private val transformation = SeparatorsTextFieldBufferTransformation()

    override fun TextFieldBuffer.transformOutput() {
        transformation.transformOutput(this, separators)
    }
}
