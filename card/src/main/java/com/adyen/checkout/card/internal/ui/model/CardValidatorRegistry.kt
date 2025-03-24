/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.R
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.card.internal.util.CardAddressValidationUtils
import com.adyen.checkout.card.internal.util.CardValidationUtils
import com.adyen.checkout.card.internal.util.KcpValidationUtils
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.validation.DefaultValidator
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidator
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import com.adyen.checkout.ui.core.internal.util.SocialSecurityNumberUtils

internal class CardValidatorRegistry(
    // TODO: We might need to move this to a specific validator
    private val validationMapper: CardValidationMapper = CardValidationMapper()
) : FieldValidatorRegistry<CardFieldId, CardDelegateState> {

    private val validators = CardFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            CardFieldId.CARD_NUMBER -> CardNumberValidator(validationMapper)
            CardFieldId.SELECTED_CARD_INDEX -> DefaultValidator()
            CardFieldId.CARD_SECURITY_CODE -> CardSecurityCodeValidator(validationMapper)
            CardFieldId.CARD_EXPIRY_DATE -> CardExpiryDateValidator(validationMapper)
            CardFieldId.CARD_HOLDER_NAME -> CardHolderNameValidator()
            CardFieldId.SOCIAL_SECURITY_NUMBER -> SocialSecurityNumberValidator()
            CardFieldId.KCP_BIRTH_DATE_OR_TAX_NUMBER -> KcpBirthDateOrTaxNumberValidator()
            CardFieldId.KCP_CARD_PASSWORD -> KcpCardPasswordValidator()
            CardFieldId.ADDRESS_POSTAL_CODE -> AddressPostalCodeValidator()
            CardFieldId.STORED_PAYMENT_METHOD_SWITCH -> DefaultValidator()
            CardFieldId.INSTALLMENT_OPTION -> DefaultValidator()
            CardFieldId.ADDRESS_LOOKUP -> DefaultValidator()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> validate(
        key: CardFieldId,
        value: T,
        state: CardDelegateState
    ): Validation {
        val validator = validators[key] as? FieldValidator<T, CardDelegateState>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return validator.validate(value, state)
    }
}

internal class CardNumberValidator(
    private val validationMapper: CardValidationMapper
) : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        val validation = CardValidationUtils.validateCardNumber(
            input,
            // TODO: Probably we should not have default values?
            state.enableLuhnCheck,
            state.isBrandSupported ?: false,
        )
        return validationMapper.mapCardNumberValidation(validation)
    }
}

internal class CardSecurityCodeValidator(
    private val validationMapper: CardValidationMapper
) : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        val validation = CardValidationUtils.validateSecurityCode(
            input,
            state.selectedOrFirstCardType,
            state.cvcUIState,
        )
        return validationMapper.mapSecurityCodeValidation(validation)
    }
}

internal class CardExpiryDateValidator(
    private val validationMapper: CardValidationMapper
) : FieldValidator<ExpiryDate, CardDelegateState> {
    override fun validate(input: ExpiryDate, state: CardDelegateState): Validation {
        val validation = CardValidationUtils.validateExpiryDate(
            input,
            state.selectedOrFirstCardType?.expiryDatePolicy
        )
        return validationMapper.mapExpiryDateValidation(validation)
    }
}

internal class CardHolderNameValidator : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        return if (state.componentParams.isHolderNameRequired && input.isBlank()) {
            Validation.Invalid(R.string.checkout_holder_name_not_valid)
        } else {
            Validation.Valid
        }
    }
}

internal class SocialSecurityNumberValidator : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        return if (state.componentParams.socialSecurityNumberVisibility == SocialSecurityNumberVisibility.SHOW) {
            // TODO To be fixed later to take the transformation into account if necessary
            SocialSecurityNumberUtils.validateSocialSecurityNumber(input).validation
        } else {
            Validation.Valid
        }
    }
}

internal class KcpBirthDateOrTaxNumberValidator : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        return if (state.componentParams.kcpAuthVisibility == KCPAuthVisibility.SHOW) {
            KcpValidationUtils.validateKcpBirthDateOrTaxNumber(input).validation
        } else {
            Validation.Valid
        }
    }
}

internal class KcpCardPasswordValidator : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        return if (state.componentParams.kcpAuthVisibility == KCPAuthVisibility.SHOW) {
            KcpValidationUtils.validateKcpCardPassword(input).validation
        } else {
            Validation.Valid
        }
    }
}

internal class AddressPostalCodeValidator : FieldValidator<String, CardDelegateState> {
    override fun validate(input: String, state: CardDelegateState): Validation {
        val isOptional = CardAddressValidationUtils.isAddressOptional(
            addressParams = state.componentParams.addressParams,
            cardType = state.selectedOrFirstCardType?.cardBrand?.txVariant,
        )

        return AddressValidationUtils.validateAddressInput(
            // TODO: Try not to create the model here
            AddressInputModel(postalCode = input),
            state.addressFormUIState,
            state.updatedCountryOptions,
            state.updatedStateOptions,
            isOptional,
        ).postalCode.validation
    }
}
