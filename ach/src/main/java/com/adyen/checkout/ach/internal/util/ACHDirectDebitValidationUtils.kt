/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.util

import com.adyen.checkout.ach.R
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

internal object ACHDirectDebitValidationUtils {

    private const val MINIMUM_ACCOUNT_NUMBER_LENGTH = 4
    private const val MAXIMUM_ACCOUNT_NUMBER_LENGTH = 17
    private const val BANK_LOCATION_LENGTH = 9

    fun validateBankAccountNumber(accountNumber: String): FieldState<String> {
        return if (accountNumber.length in MINIMUM_ACCOUNT_NUMBER_LENGTH..MAXIMUM_ACCOUNT_NUMBER_LENGTH) {
            FieldState(value = accountNumber, validation = Validation.Valid)
        } else {
            FieldState(
                value = accountNumber,
                validation = Validation.Invalid(
                    reason = R.string.checkout_ach_bank_account_number_invalid
                )
            )
        }
    }

    fun validateBankLocationId(bankLocationId: String): FieldState<String> {
        return if (bankLocationId.length == BANK_LOCATION_LENGTH) {
            FieldState(value = bankLocationId, validation = Validation.Valid)
        } else {
            FieldState(
                value = bankLocationId,
                validation = Validation.Invalid(
                    reason = R.string.checkout_ach_bank_account_location_invalid
                )
            )
        }
    }

    fun validateOwnerName(ownerName: String): FieldState<String> {
        return if (ownerName.isNotBlank()) {
            FieldState(value = ownerName, validation = Validation.Valid)
        } else {
            FieldState(
                value = ownerName,
                validation = Validation.Invalid(
                    reason = R.string.checkout_ach_bank_account_holder_name_invalid
                )
            )
        }
    }
}
