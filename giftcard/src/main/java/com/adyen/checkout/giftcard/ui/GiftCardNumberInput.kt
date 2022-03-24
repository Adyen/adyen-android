/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard.ui

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.giftcard.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.util.GiftCardNumberUtils.DIGIT_SEPARATOR
import com.adyen.checkout.giftcard.util.GiftCardNumberUtils.MAXIMUM_GIFT_CARD_NUMBER_LENGTH
import com.adyen.checkout.giftcard.util.GiftCardNumberUtils.MAX_DIGIT_SEPARATOR_COUNT

class GiftCardNumberInput constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    companion object {
        private const val SUPPORTED_DIGITS = "0123456789"
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        enforceMaxInputLength(MAXIMUM_GIFT_CARD_NUMBER_LENGTH + MAX_DIGIT_SEPARATOR_COUNT)
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance(SUPPORTED_DIGITS + DIGIT_SEPARATOR)
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
}
