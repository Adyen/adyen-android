/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.adyen.checkout.card.internal.util.SocialSecurityNumberUtils
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText

class SocialSecurityNumberInput constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    companion object {
        private const val SUPPORTED_CHARS = "0123456789./-"
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        enforceMaxInputLength(
            SocialSecurityNumberUtils.CNPJ_DIGIT_LIMIT + SocialSecurityNumberUtils.CNPJ_MASK_SEPARATORS.size
        )
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance(SUPPORTED_CHARS)
    }

    override fun afterTextChanged(editable: Editable) {
        val original = editable.toString()
        val formatted = SocialSecurityNumberUtils.formatInput(original)
        if (formatted != original) {
            editable.replace(0, original.length, formatted)
        }
        super.afterTextChanged(editable)
    }
}
