/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui.model

import com.adyen.checkout.core.internal.ui.state.model.Validation
import com.adyen.checkout.core.internal.ui.state.validator.DefaultValidator
import com.adyen.checkout.core.internal.ui.state.validator.FieldValidator
import com.adyen.checkout.core.internal.ui.state.validator.FieldValidatorRegistry
import com.adyen.checkout.core.mbway.internal.ui.state.MBWayFieldId

internal class MBWayValidatorRegistry : FieldValidatorRegistry<MBWayDelegateState, MBWayFieldId> {

    private val validators = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.PHONE_NUMBER -> LocalPhoneNumberValidator()
            MBWayFieldId.COUNTRY_CODE -> DefaultValidator()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> validate(state: MBWayDelegateState, key: MBWayFieldId, value: T): Validation {
        val validator = validators[key] as? FieldValidator<MBWayDelegateState, T>
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
