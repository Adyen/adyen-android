/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard.internal.ui.view

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.annotation.RestrictTo
import androidx.autofill.HintConstants
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils.DIGIT_SEPARATOR
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils.MAXIMUM_GIFT_CARD_NUMBER_LENGTH
import com.adyen.checkout.giftcard.internal.util.GiftCardNumberUtils.MAX_DIGIT_SEPARATOR_COUNT
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText

class GiftCardNumberInput
@JvmOverloads
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    init {
        enforceMaxInputLength(MAXIMUM_GIFT_CARD_NUMBER_LENGTH + MAX_DIGIT_SEPARATOR_COUNT)
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance(SUPPORTED_DIGITS + DIGIT_SEPARATOR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setAutofillHints(HintConstants.AUTOFILL_HINT_GIFT_CARD_NUMBER)
        }
    }

    override val rawValue: String
        get() = GiftCardNumberUtils.getRawValue(text.toString())

    override fun afterTextChanged(editable: Editable) {
        val original = editable.toString()
        val formatted = GiftCardNumberUtils.formatInput(original)
        if (formatted != original) {
            editable.replace(0, original.length, formatted)
        }
        super.afterTextChanged(editable)
    }

    companion object {
        private const val SUPPORTED_DIGITS = "0123456789"
    }
}
