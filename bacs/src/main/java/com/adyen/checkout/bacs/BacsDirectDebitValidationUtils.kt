/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/11/2021.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.ValidationUtils

private const val BANK_ACCOUNT_NUMBER_LENGTH = 8
private const val SORT_CODE_LENGTH = 6

object BacsDirectDebitValidationUtils {

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
