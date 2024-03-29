/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.bacs.internal.ui

import com.adyen.checkout.bacs.R
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.ValidationUtils

private const val BANK_ACCOUNT_NUMBER_LENGTH = 8
private const val SORT_CODE_LENGTH = 6

internal object BacsDirectDebitValidationUtils {

    fun validateHolderName(holderName: String): FieldState<String> {
        return if (holderName.isBlank()) {
            FieldState(holderName, Validation.Invalid(R.string.bacs_holder_name_invalid))
        } else {
            FieldState(holderName, Validation.Valid)
        }
    }

    fun validateBankAccountNumber(bankAccountNumber: String): FieldState<String> {
        return if (bankAccountNumber.length == BANK_ACCOUNT_NUMBER_LENGTH) {
            FieldState(bankAccountNumber, Validation.Valid)
        } else {
            FieldState(bankAccountNumber, Validation.Invalid(R.string.bacs_account_number_invalid))
        }
    }

    fun validateSortCode(sortCode: String): FieldState<String> {
        return if (sortCode.length == SORT_CODE_LENGTH) {
            FieldState(sortCode, Validation.Valid)
        } else {
            FieldState(sortCode, Validation.Invalid(R.string.bacs_sort_code_invalid))
        }
    }

    fun validateShopperEmail(shopperEmail: String): FieldState<String> {
        return if (ValidationUtils.isEmailValid(shopperEmail)) {
            FieldState(shopperEmail, Validation.Valid)
        } else {
            FieldState(shopperEmail, Validation.Invalid(R.string.bacs_shopper_email_invalid))
        }
    }
}
