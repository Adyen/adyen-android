package com.adyen.checkout.card.ui

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText

class SocialSecurityNumberInput constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AdyenTextInputEditText(context, attrs, defStyleAttr) {

    companion object {

        private const val CPF_MAX_DIGITS = 11
        private const val CNPJ_MAX_DIGITS = 14
        private val CPF_MASK_GROUPING = listOf(3, 3, 3, 2) // e.g 123.123.123-12
        private val CNPJ_MASK_GROUPING = listOf(2, 3, 3, 4, 2) // e.g 12.123.123/1234-12
        private val CPF_MASK_SEPARATORS = listOf('.', '.', '-')
        private val CNPJ_MASK_SEPARATORS = listOf('.', '.', '/', '-')
        private const val STRING_START_INDEX = 0
        private const val SUPPORTED_CHARS = "0123456789./-"
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        enforceMaxInputLength(CNPJ_MAX_DIGITS + CNPJ_MASK_SEPARATORS.size)
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance(SUPPORTED_CHARS)
    }

    override fun afterTextChanged(editable: Editable) {
        val original = editable.toString()
        val formatted = formatString(original)
        if (formatted != original) {
            editable.replace(STRING_START_INDEX, original.length, formatted)
        }
        super.afterTextChanged(editable)
    }

    private fun formatString(originalInput: String): String {
        var trimmedInput = originalInput.filter { it.isDigit() }
        val result = mutableListOf<String>()
        val resultBuilder = StringBuilder()

        val (grouping, separators) = if (trimmedInput.length <= CPF_MAX_DIGITS) {
            CPF_MASK_GROUPING to CPF_MASK_SEPARATORS
        } else {
            CNPJ_MASK_GROUPING to CNPJ_MASK_SEPARATORS
        }

        for (index in grouping.indices) {
            if (trimmedInput.length >= grouping[index]) {
                result.add(trimmedInput.substring(STRING_START_INDEX, grouping[index]))
                trimmedInput = trimmedInput.substring(grouping[index])
            } else if (trimmedInput.isNotEmpty()) {
                result.add(trimmedInput)
                break
            }
        }

        result.forEachIndexed { index, resultPart ->
            resultBuilder.append(resultPart)
            if (index != result.lastIndex) {
                resultBuilder.append(separators[index])
            }
        }

        return resultBuilder.toString()
    }
}
