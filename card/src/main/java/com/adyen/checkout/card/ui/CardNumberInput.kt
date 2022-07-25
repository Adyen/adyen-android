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
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.adyen.checkout.card.util.CardValidationUtils
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import java.util.Arrays

/**
 * Input that support formatting for card number.
 */
open class CardNumberInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenTextInputEditText(context, attrs, defStyleAttr) {

    private var isAmexCard = false
    override val rawValue: String
        get() = text.toString().replace(DIGIT_SEPARATOR.toString(), "")

    init {
        enforceMaxInputLength(CardValidationUtils.MAXIMUM_CARD_NUMBER_LENGTH + MAX_DIGIT_SEPARATOR_COUNT)
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
        var processed = initial.trim { it <= ' ' }.replace(DIGIT_SEPARATOR.toString().toRegex(), "")
        processed = formatProcessedString(processed)
        if (initial != processed) {
            editable.replace(0, initial.length, processed)
        }
        super.afterTextChanged(editable)
    }

    @Suppress("SpreadOperator")
    private fun formatProcessedString(processedValue: String): String {
        val result =
            splitStringWithMask(processedValue, *if (isAmexCard) AMEX_CARD_NUMBER_MASK else DEFAULT_CARD_NUMBER_MASK)
        return result.joinToString(DIGIT_SEPARATOR.toString()).trim { it <= ' ' }
    }

    private fun splitStringWithMask(value: String, vararg mask: Int): Array<String?> {
        val result = arrayOfNulls<String>(mask.size)
        Arrays.fill(result, "")
        var tempValue = value
        for (indexOfMask in mask.indices) {
            if (tempValue.length >= mask[indexOfMask]) {
                result[indexOfMask] = tempValue.substring(START_OF_STRING, mask[indexOfMask])
                tempValue = tempValue.substring(mask[indexOfMask])
            } else {
                result[indexOfMask] = tempValue
                break
            }
        }
        return result
    }

    companion object {
        private const val MAX_DIGIT_SEPARATOR_COUNT = 4
        private const val DIGIT_SEPARATOR = ' '
        private const val SUPPORTED_DIGITS = "0123456789"
        private val AMEX_CARD_NUMBER_MASK = intArrayOf(4, 6, 5, 4)
        private val DEFAULT_CARD_NUMBER_MASK = intArrayOf(4, 4, 4, 4, 3)
        private const val START_OF_STRING = 0
    }
}
