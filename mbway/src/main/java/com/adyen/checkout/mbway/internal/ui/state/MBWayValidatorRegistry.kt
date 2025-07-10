/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.Validation
import com.adyen.checkout.core.components.internal.ui.state.validator.DefaultValidator
import com.adyen.checkout.core.components.internal.ui.state.validator.FieldValidator
import com.adyen.checkout.core.components.internal.ui.state.validator.FieldValidatorRegistry

internal class MBWayValidatorRegistry : FieldValidatorRegistry<MBWayDelegateState, MBWayFieldId> {

    private val validators = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.PHONE_NUMBER -> LocalPhoneNumberValidator()
            MBWayFieldId.COUNTRY_CODE -> DefaultValidator()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> validate(state: MBWayDelegateState, fieldId: MBWayFieldId, value: T): Validation {
        val validator = validators[fieldId] as? FieldValidator<MBWayDelegateState, T>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return validator.validate(state, value)
    }
}

internal class LocalPhoneNumberValidator : FieldValidator<MBWayDelegateState, String> {
    override fun validate(state: MBWayDelegateState, input: String) =
        // TODO Do proper validation like ValidationUtils.isPhoneNumberValid(input)
        if (input.isNotEmpty()) {
            Validation.Valid
        } else {
            Validation.Invalid(0) // TODO Pass a correct invalid resource ID
        }
}
