/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 20/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete

/**
 * Caps the text at [maxLength] the way a browser's `maxlength` does: inserted text is cut to fit, so a long paste or
 * autofill keeps what fits instead of being dropped.
 *
 * An edit that does not make the text longer is always kept. The maximum can shrink while text is already entered,
 * for example when the country changes, and the shopper must still be able to delete it.
 *
 * The inserted text is found by comparing the edit with the original text: it is what lies between their shared start
 * and their shared end. The length counts UTF-16 units, so a cut never splits a surrogate pair (an emoji such as 😀):
 * half of one would pass the character validation unseen, so the whole character goes.
 */
internal class TruncatingMaxLengthInputTransformation(
    private val maxLength: Int,
) : InputTransformation {

    override fun TextFieldBuffer.transformInput() {
        val overflow = length - maxLength
        if (overflow <= 0 || length <= originalText.length) return

        val text = asCharSequence()
        val insertionStart = text.commonPrefixWith(originalText).length
        val sharedEnd = minOf(text.commonSuffixWith(originalText).length, originalText.length - insertionStart)
        val insertionEnd = length - sharedEnd
        val cutStart = insertionEnd - minOf(overflow, insertionEnd - insertionStart)
        val splitsPair = cutStart > insertionStart &&
            text[cutStart - 1].isHighSurrogate() &&
            text[cutStart].isLowSurrogate()
        delete(if (splitsPair) cutStart - 1 else cutStart, insertionEnd)
    }
}
