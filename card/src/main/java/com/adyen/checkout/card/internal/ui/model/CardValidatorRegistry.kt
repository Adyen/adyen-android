/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.state.ValidationContext
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidator
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry

internal class CardValidatorRegistry : FieldValidatorRegistry<CardFieldId> {

    private val validators = CardFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            CardFieldId.CARD_NUMBER -> CardNumberValidator()
//            CardFieldId.CARD_EXPIRY_DATE -> TODO()
//            CardFieldId.CARD_SECURITY_CODE -> TODO()
//            CardFieldId.CARD_HOLDER_NAME -> TODO()
//            CardFieldId.ADDRESS_POSTAL_CODE -> TODO()
//            CardFieldId.ADDRESS_LOOKUP -> TODO()
//            CardFieldId.SOCIAL_SECURITY_NUMBER -> TODO()
//            CardFieldId.BIRTH_DATE_OR_TAX_NUMBER -> TODO()
//            CardFieldId.CARD_PASSWORD -> TODO()
//            CardFieldId.INSTALLMENTS -> TODO()
//            CardFieldId.STORE_PAYMENT_SWITCH -> TODO()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> validate(
        key: CardFieldId,
        value: T,
        validationContext: ValidationContext?
    ): Validation {
        val validator = validators[key] as? FieldValidator<T>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return validator.validate(value, validationContext)
    }
}

internal class CardNumberValidator : FieldValidator<String> {
    override fun validate(input: String, context: ValidationContext?): Validation {
        val cardContext = context as? CardValidationContext
            ?: throw IllegalArgumentException("CardValidationContext required")

        val validation =
            CardValidationUtils.validateCardNumber(
                input,
                cardContext.enableLuhnCheck,
                cardContext.isBrandSupported
            )
        return cardContext.validationMapper.mapCardNumberValidation(validation)
    }
}

// TODO: Could this move somewhere else?
internal data class CardValidationContext(
    val enableLuhnCheck: Boolean,
    val isBrandSupported: Boolean,
    val validationMapper: CardValidationMapper,
) : ValidationContext
