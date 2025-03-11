/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.state.ValidationContext
import com.adyen.checkout.components.core.internal.ui.model.validation.DefaultValidator
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidator
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.mbway.R

internal class MBWayValidatorRegistry : FieldValidatorRegistry<MBWayFieldId> {

    private val validators = MBWayFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            MBWayFieldId.COUNTRY_CODE -> DefaultValidator()
            MBWayFieldId.LOCAL_PHONE_NUMBER -> LocalPhoneNumberValidator()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> validate(
        key: MBWayFieldId,
        value: T,
        validationContext: ValidationContext?
    ): Validation {
        val validator = validators[key] as? FieldValidator<T>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return validator.validate(value, validationContext)
    }
}

internal class LocalPhoneNumberValidator : FieldValidator<String> {
    override fun validate(input: String, context: ValidationContext?) =
        if (input.isNotEmpty() && ValidationUtils.isPhoneNumberValid(input)) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_mbway_phone_number_not_valid)
        }
}
