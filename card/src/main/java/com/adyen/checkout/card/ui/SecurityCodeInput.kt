/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/3/2022.
 */
package com.adyen.checkout.card.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet

class SecurityCodeInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardNumberInput(context, attrs, defStyleAttr) {

    init {
        enforceMaxInputLength(MAX_LENGTH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setAutofillHints(AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE)
        }
    }

    companion object {
        private const val MAX_LENGTH = 4
    }
}
