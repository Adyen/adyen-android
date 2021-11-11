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

object BacsDirectDebitValidationUtils {

    fun validateHolderName(holderName: String): FieldState<String> {
        return if (holderName.isBlank()) {
            FieldState(holderName, Validation.Invalid(0))
        } else {
            FieldState(holderName, Validation.Valid)
        }
    }

    fun validateBankAccountNumber(bankAccountNumber: String): FieldState<String> {

    }

    fun validateSortCode(sortCode: String): FieldState<String> {

    }

    fun validateShopperEmail(shopperEmail: String): FieldState<String> {

    }
}