/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/3/2022.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.annotation.RestrictTo
import com.adyen.checkout.card.internal.util.CardNumberUtils
import com.adyen.checkout.core.old.ui.validation.CardNumberValidator
import com.adyen.checkout.ui.core.old.internal.ui.view.AdyenTextInputEditText

/**
 * Input that support formatting for card number.
 */
class CardNumberInput
@JvmOverloads
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    private var isAmexCard = false
    override val rawValue: String
        get() = text.toString().replace(DIGIT_SEPARATOR, "")

    init {
        enforceMaxInputLength(CardNumberValidator.MAXIMUM_CARD_NUMBER_LENGTH + MAX_DIGIT_SEPARATOR_COUNT)
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance(SUPPORTED_DIGITS + DIGIT_SEPARATOR)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setAutofillHints(AUTOFILL_HINT_CREDIT_CARD_NUMBER)
        }
    }

    /**
     * Enable Amex formatting.
     */
    fun setAmexCardFormat(value: Boolean) {
        // first time detecting is Amex card
        if (!isAmexCard && value) {
            isAmexCard = true
            afterTextChanged(editableText)
            return
        }
        isAmexCard = value
    }

    override fun afterTextChanged(editable: Editable) {
        val initial = editable.toString()
        var processed = initial.replace(DIGIT_SEPARATOR, "")
        processed = formatProcessedString(processed)
        if (initial != processed) {
            editable.replace(0, initial.length, processed)
        }
        super.afterTextChanged(editable)
    }

    private fun formatProcessedString(unformattedString: String): String {
        return CardNumberUtils.formatCardNumber(
            unformattedString = unformattedString,
            maskPartsLengths = if (isAmexCard) AMEX_CARD_NUMBER_MASK else DEFAULT_CARD_NUMBER_MASK,
            separator = DIGIT_SEPARATOR,
        )
    }

    companion object {
        private const val MAX_DIGIT_SEPARATOR_COUNT = 4
        private const val DIGIT_SEPARATOR = " "
        private const val SUPPORTED_DIGITS = "0123456789"

        // 4 characters then 6 then 5 then 4. Example: 1234 123456 12345 1234
        private val AMEX_CARD_NUMBER_MASK = listOf(4, 6, 5, 4)

        // 4 characters then 4 then 4 then 4 then 3. Example: 1234 1234 1234 1234 123
        private val DEFAULT_CARD_NUMBER_MASK = listOf(4, 4, 4, 4, 3)
    }
}
