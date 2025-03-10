/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.card.internal.util.CardValidationUtils.validateCardNumber
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidator
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.core.ui.validation.CardNumberValidator

internal class CardValidatorRegistry : FieldValidatorRegistry<CardFieldId> {

    private val validators = CardFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            CardFieldId.CARD_NUMBER -> CardNumberValidator()
            CardFieldId.CARD_EXPIRY_DATE -> TODO()
            CardFieldId.CARD_SECURITY_CODE -> TODO()
            CardFieldId.CARD_HOLDER_NAME -> TODO()
            CardFieldId.ADDRESS_POSTAL_CODE -> TODO()
            CardFieldId.ADDRESS_LOOKUP -> TODO()
            CardFieldId.SOCIAL_SECURITY_NUMBER -> TODO()
            CardFieldId.BIRTH_DATE_OR_TAX_NUMBER -> TODO()
            CardFieldId.CARD_PASSWORD -> TODO()
            CardFieldId.INSTALLMENTS -> TODO()
            CardFieldId.STORE_PAYMENT_SWITCH -> TODO()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> validate(key: CardFieldId, value: T): Validation {
        val validator = validators[key] as? FieldValidator<T>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return validator.validate(value)
    }
}

internal class CardNumberValidator: FieldValidator<String> {
    override fun validate(input: String): Validation {
        val validation = CardValidationUtils.validateCardNumber(input, enableLuhnCheck, isBrandSupported)
        return cardValidationMapper.mapCardNumberValidation(input, validation)
    }
}
