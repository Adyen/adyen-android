/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.view

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/**
 * Turns line breaks and tabs into spaces as they are entered, for example from a pasted or autofilled two-line street.
 *
 * A single-line text field only hides line breaks on screen and keeps them in its text. Most are control characters,
 * which the character validation would reject with nothing visible to fix, and the Unicode line and paragraph
 * separators would otherwise be submitted. Browsers also turn pasted line breaks into spaces in a single-line input.
 */
internal object LineBreakToSpaceInputTransformation : InputTransformation {

    // \r\n first, so a Windows line break becomes one space. The class lists every Unicode line break, plus the tab.
    private val LINE_BREAK = Regex("\r\n|[\t\n\u000B\u000C\r\u0085\u2028\u2029]")

    override fun TextFieldBuffer.transformInput() {
        LINE_BREAK.findAll(asCharSequence()).toList().asReversed().forEach { match ->
            replace(match.range.first, match.range.last + 1, " ")
        }
    }
}
