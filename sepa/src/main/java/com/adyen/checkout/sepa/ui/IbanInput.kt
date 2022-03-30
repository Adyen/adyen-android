/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2019.
 */
package com.adyen.checkout.sepa.ui

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.sepa.Iban

class IbanInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenTextInputEditText(context, attrs, defStyleAttr) {

    override fun afterTextChanged(editable: Editable) {
        val initial = editable.toString()
        val processed = Iban.format(initial)
        if (initial != processed) {
            editable.replace(0, initial.length, processed)
        }
        super.afterTextChanged(editable)
    }

    init {
        enforceMaxInputLength(Iban.formattedMaxLength)
    }
}
