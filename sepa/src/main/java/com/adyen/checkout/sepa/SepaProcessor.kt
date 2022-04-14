/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/4/2022.
 */

package com.adyen.checkout.sepa

import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation

object SepaProcessor {

    operator fun invoke(sepaInputData: SepaInputData) : SepaOutputData {
        val ownerNameField: FieldState<String> = with(sepaInputData.name) {
            FieldState(
                value = this,
                validation = validateName(this),
            )
        }
        val iban: Iban? = Iban.parse(sepaInputData.iban)
        val ibanNumberField: FieldState<String> = validateIbanNumber(sepaInputData.iban, iban)

        val isValid = ownerNameField.validation.isValid() && ibanNumberField.validation.isValid()

        return SepaOutputData(
            ownerNameField = ownerNameField,
            ibanNumberField = ibanNumberField,
            iban = iban,
            isValid = isValid
        )
    }

    private fun validateName(name: String) = if (name.isEmpty())
        Validation.Invalid(R.string.checkout_holder_name_not_valid)
    else
        Validation.Valid

    private fun validateIbanNumber(ibanNumber: String, iban: Iban?): FieldState<String> {
        val validation: Validation = if (iban != null) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_iban_not_valid)
        }
        return FieldState(ibanNumber, validation)
    }
}
