/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/7/2024.
 */
package com.adyen.checkout.ui.core.internal.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import androidx.annotation.RestrictTo

class ExpiryDateInput
@JvmOverloads
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    init {
        enforceMaxInputLength(MAX_LENGTH)
    }

    public override fun afterTextChanged(editable: Editable) {
        val initial = editable.toString()
        // remove digits
        var processed = initial.replace("\\D".toRegex(), "")
        // add separator
        processed = processed.replace("(\\d{2})(?=\\d)".toRegex(), "$1$SEPARATOR")
        // add tailing zero to month
        if (processed.length == 1 && isStringInt(processed) && processed.toInt() > MAX_SECOND_DIGIT_MONTH) {
            processed = "0$processed"
        }
        if (initial != processed) {
            editable.replace(0, initial.length, processed)
        }
        super.afterTextChanged(editable)
    }

    private fun isStringInt(s: String): Boolean {
        return try {
            s.toInt()
            true
        } catch (ex: NumberFormatException) {
            false
        }
    }

    companion object {
        const val SEPARATOR = "/"
        private const val MAX_LENGTH = 5
        private const val MAX_SECOND_DIGIT_MONTH = 1
    }
}
