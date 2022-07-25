/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.sepa

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.sepa.Iban.Companion.parse

class SepaOutputData(ownerName: String, ibanNumber: String) : OutputData {

    val ownerNameField: FieldState<String> = FieldState(
        value = ownerName,
        validation = if (ownerName.isEmpty())
            Validation.Invalid(R.string.checkout_holder_name_not_valid)
        else
            Validation.Valid
    )
    val ibanNumberField: FieldState<String>
    val iban: Iban? = parse(ibanNumber)

    override val isValid: Boolean
        get() = ownerNameField.validation.isValid() && ibanNumberField.validation.isValid()

    private fun validateIbanNumber(ibanNumber: String, iban: Iban?): FieldState<String> {
        val validation: Validation = if (iban != null) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_iban_not_valid)
        }
        return FieldState(ibanNumber, validation)
    }

    init {
        ibanNumberField = validateIbanNumber(ibanNumber, iban)
    }
}
