/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.util

import com.adyen.checkout.card.R
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.DateUtils

internal object KcpValidationUtils {

    // KCP
    const val KCP_BIRTH_DATE_LENGTH = 6
    private const val KCP_BIRTH_DATE_FORMAT = "yyMMdd"
    private const val KCP_TAX_NUMBER_LENGTH = 10
    private const val KCP_CARD_PASSWORD_REQUIRED_LENGTH = 2

    /**
     * Validate KCP input birth date or tax number.
     */
    fun validateKcpBirthDateOrTaxNumber(birthDateOrTaxNumber: String): FieldState<String> {
        val inputLength = birthDateOrTaxNumber.length
        val validation = when {
            inputLength == KCP_BIRTH_DATE_LENGTH &&
                DateUtils.matchesFormat(birthDateOrTaxNumber, KCP_BIRTH_DATE_FORMAT)
            -> Validation.Valid

            inputLength == KCP_TAX_NUMBER_LENGTH -> Validation.Valid
            else -> Validation.Invalid(R.string.checkout_kcp_birth_date_or_tax_number_invalid)
        }
        return FieldState(birthDateOrTaxNumber, validation)
    }

    /**
     * Validate KCP input card password.
     */
    fun validateKcpCardPassword(cardPassword: String): FieldState<String> {
        val validation = when (cardPassword.length) {
            KCP_CARD_PASSWORD_REQUIRED_LENGTH -> Validation.Valid
            else -> Validation.Invalid(R.string.checkout_kcp_password_invalid)
        }
        return FieldState(cardPassword, validation)
    }
}
