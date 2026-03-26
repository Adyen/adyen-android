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
import androidx.compose.foundation.text.input.insert

/**
 * Handles the logic of [SeparatorsOutputTransformation] which allows custom OutputTransformation classes to reuse this
 * logic and extend it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SeparatorsTextFieldBufferTransformation {

    fun transformOutput(
        textFieldBuffer: TextFieldBuffer,
        separators: List<TextFieldSeparator>,
    ) = with(textFieldBuffer) {
        val rawLength = length

        if (separators.distinctBy { it.indexInRawString }.size != separators.size) {
            error("separators list should not have any duplicate indexes")
        }

        separators.sortedBy { it.indexInRawString }.forEachIndexed { listIndex, separator ->
            if (rawLength > separator.indexInRawString) {
                // we need to add the listIndex to the indexInRawString to account for the already added separators
                insert(separator.indexInRawString + listIndex, separator.character.toString())
            }
        }
    }
}
