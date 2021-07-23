package com.adyen.checkout.card

import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import java.util.regex.Pattern

object SocialSecurityNumberValidationUtils {

    private const val CPF_DIGIT_LIMIT = 11
    private const val CPF_REGEX = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}"
    private val CPF_PATTERN = Pattern.compile(CPF_REGEX)

    private const val CNPJ_DIGIT_LIMIT = 14
    private const val CNPJ_REGEX = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}"
    private val CNPJ_PATTERN = Pattern.compile(CNPJ_REGEX)

    fun validateSocialSecurityNumber(socialSecurityNumber: String): FieldState<String> {
        val digitLength = socialSecurityNumber.filter { it.isDigit() }.length
        val invalidState = Validation.Invalid(R.string.checkout_social_security_number_not_valid)
        val validation = when {
            digitLength < CPF_DIGIT_LIMIT -> invalidState
            digitLength == CPF_DIGIT_LIMIT && CPF_PATTERN.matcher(socialSecurityNumber).matches() -> Validation.Valid
            digitLength < CNPJ_DIGIT_LIMIT -> invalidState
            digitLength == CNPJ_DIGIT_LIMIT && CNPJ_PATTERN.matcher(socialSecurityNumber).matches() -> Validation.Valid
            else -> invalidState
        }
        return FieldState(socialSecurityNumber, validation)
    }
}