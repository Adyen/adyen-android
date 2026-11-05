/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.properties.AddressCharacters
import com.adyen.checkout.address.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.model.PickerInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.requiresValidation

internal class AddressComponentStateValidator : ComponentStateValidator<AddressComponentState> {

    override fun validate(state: AddressComponentState): AddressComponentState = state.copy(
        country = state.country.updateError(
            state.validateSelection(
                id = AddressFormElementId.COUNTRY,
                picker = state.country,
                error = CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR,
            ),
        ),
        street = state.street.updateError(validateAddressLine(state.street)),
        houseNumberOrName = state.houseNumberOrName.updateError(validateAddressLine(state.houseNumberOrName)),
        postalCode = state.postalCode.updateError(validatePostalCode(state.postalCode, state.country.selected)),
        city = state.city.updateError(validateAddressLine(state.city)),
        stateOrProvince = state.stateOrProvince.updateError(
            state.validateSelection(
                id = AddressFormElementId.STATE_OR_PROVINCE,
                picker = state.stateOrProvince,
                error = CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR,
            ),
        ),
    )

    override fun isValid(state: AddressComponentState): Boolean = state.form.isFormValid

    /**
     * A blank postal code is invalid. Otherwise it must contain the selected country's pattern, matched unanchored as
     * on Web; a country without a pattern accepts any value.
     */
    private fun validatePostalCode(
        postalCode: TextInputComponentState,
        countryCode: String?,
    ): CheckoutLocalizationKey? {
        val pattern = PostalCodeProperties.getPattern(countryCode)
        val isValid = when {
            !postalCode.requiresValidation() -> true
            postalCode.text.isBlank() -> false
            pattern == null -> true
            else -> pattern.containsMatchIn(postalCode.text.trim())
        }
        return CheckoutLocalizationKey.GENERAL_FIELD_ERROR.takeUnless { isValid }
    }

    /** A picker the spec requires needs a selection; an optional or hidden one never has an error. */
    private fun AddressComponentState.validateSelection(
        id: AddressFormElementId,
        picker: PickerInputComponentState<*>,
        error: CheckoutLocalizationKey,
    ): CheckoutLocalizationKey? {
        val isRequired = spec.getFieldSpec(id).requirementPolicy == RequirementPolicy.Required
        return error.takeIf { isRequired && picker.selected == null }
    }

    /**
     * The street, the house number and the city: required as their spec says, and free of emoji and control
     * characters. An empty value only gets the required error.
     */
    private fun validateAddressLine(input: TextInputComponentState): CheckoutLocalizationKey? =
        validateRequiredText(input)
            ?: CheckoutLocalizationKey.ADDRESS_INVALID_CHARACTERS_ERROR.takeIf {
                AddressCharacters.containsInvalidCharacters(input.text)
            }

    /** An optional field is only validated when it has content, so a blank optional field is valid. */
    private fun validateRequiredText(input: TextInputComponentState): CheckoutLocalizationKey? =
        CheckoutLocalizationKey.GENERAL_FIELD_ERROR.takeIf { input.requiresValidation() && input.text.isBlank() }
}
