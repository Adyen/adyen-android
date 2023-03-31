/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 14/3/2023.
 */

package com.adyen.checkout.boleto.internal.util

import com.adyen.checkout.boleto.R
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.ValidationUtils

internal object BoletoValidationUtils {

    fun validateFirstName(firstName: String): FieldState<String> {
        return if (firstName.isNotBlank()) {
            FieldState(firstName, Validation.Valid)
        } else {
            FieldState(firstName, Validation.Invalid(R.string.checkout_boleto_first_name_invalid))
        }
    }

    fun validateLastName(lastName: String): FieldState<String> {
        return if (lastName.isNotBlank()) {
            FieldState(lastName, Validation.Valid)
        } else {
            FieldState(lastName, Validation.Invalid(R.string.checkout_boleto_last_name_invalid))
        }
    }

    fun validateShopperEmail(isEmailEnabled: Boolean, shopperEmail: String): FieldState<String> {
        return if (!isEmailEnabled || ValidationUtils.isEmailValid(shopperEmail)) {
            FieldState(shopperEmail, Validation.Valid)
        } else {
            FieldState(shopperEmail, Validation.Invalid(R.string.checkout_boleto_email_invalid))
        }
    }
}
