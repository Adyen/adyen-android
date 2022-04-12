package com.adyen.checkout.card.util

import com.adyen.checkout.card.R
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import java.util.regex.Pattern

@Suppress("MagicNumber")
object SocialSecurityNumberUtils {

    private const val CPF_DIGIT_LIMIT = 11
    private const val CPF_REGEX = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}"
    private val CPF_PATTERN = Pattern.compile(CPF_REGEX)
    private val CPF_MASK_GROUPING = listOf(3, 3, 3, 2) // e.g 123.123.123-12
    private val CPF_MASK_SEPARATORS = listOf('.', '.', '-')

    const val CNPJ_DIGIT_LIMIT = 14
    private const val CNPJ_REGEX = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}"
    private val CNPJ_PATTERN = Pattern.compile(CNPJ_REGEX)
    private val CNPJ_MASK_GROUPING = listOf(2, 3, 3, 4, 2) // e.g 12.123.123/1234-12
    val CNPJ_MASK_SEPARATORS = listOf('.', '.', '/', '-')

    fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String> {
        val digitLength = socialSecurityNumber.filter { it.isDigit() }.length
        val validation = when {
            digitLength == CPF_DIGIT_LIMIT && CPF_PATTERN.matcher(socialSecurityNumber).matches() -> Validation.Valid
            digitLength == CNPJ_DIGIT_LIMIT && CNPJ_PATTERN.matcher(socialSecurityNumber).matches() -> Validation.Valid
            else -> Validation.Invalid(R.string.checkout_social_security_number_not_valid)
        }
        return FieldState(socialSecurityNumber.filter { it.isDigit() }, validation)
    }

    fun formatInput(inputString: String): String {
        var trimmedInput = inputString.filter { it.isDigit() }
        val result = mutableListOf<String>()
        val resultBuilder = StringBuilder()

        val (grouping, separators) = if (trimmedInput.length <= CPF_DIGIT_LIMIT) {
            CPF_MASK_GROUPING to CPF_MASK_SEPARATORS
        } else {
            CNPJ_MASK_GROUPING to CNPJ_MASK_SEPARATORS
        }

        for (index in grouping.indices) {
            if (trimmedInput.length >= grouping[index]) {
                result.add(trimmedInput.take(grouping[index]))
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
