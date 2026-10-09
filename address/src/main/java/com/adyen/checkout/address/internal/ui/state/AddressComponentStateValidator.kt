/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.requiresValidation

internal class AddressComponentStateValidator : ComponentStateValidator<AddressComponentState> {

    override fun validate(state: AddressComponentState): AddressComponentState = state.copy(
        country = state.country.updateError(
            validateSelection(state.country, CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR),
        ),
        street = state.street.updateError(
            validateRequiredText(state.street, CheckoutLocalizationKey.ADDRESS_STREET_ERROR),
        ),
        houseNumberOrName = state.houseNumberOrName.updateError(
            validateRequiredText(state.houseNumberOrName, CheckoutLocalizationKey.ADDRESS_HOUSE_NUMBER_ERROR),
        ),
        postalCode = state.postalCode.updateError(validatePostalCode(state.postalCode, state.country.selected)),
        city = state.city.updateError(
            validateRequiredText(state.city, CheckoutLocalizationKey.ADDRESS_CITY_ERROR),
        ),
        stateOrProvince = validateStateOrProvince(state.stateOrProvince),
    )

    override fun isValid(state: AddressComponentState): Boolean = state.form.isFormValid

    /** Loading and unavailable states are not in the form, so they are never validated. */
    private fun validateStateOrProvince(stateOrProvince: StateOrProvinceState): StateOrProvinceState =
        when (stateOrProvince) {
            is StateOrProvinceState.Options -> stateOrProvince.copy(
                picker = stateOrProvince.picker.updateError(
                    validateSelection(stateOrProvince.picker, CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR),
                ),
            )

            // The free text stands in for a required picker, so it is required too.
            is StateOrProvinceState.FreeText -> stateOrProvince.copy(
                input = stateOrProvince.input.updateError(
                    validateRequiredText(
                        stateOrProvince.input,
                        CheckoutLocalizationKey.ADDRESS_STATE_OR_PROVINCE_ERROR_FREE_TEXT,
                    ),
                ),
            )

            is StateOrProvinceState.Loading,
            StateOrProvinceState.Unavailable -> stateOrProvince
        }

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
        return CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR.takeUnless { isValid }
    }

    private fun validateSelection(
        picker: PickerInputComponentState<*>,
        error: CheckoutLocalizationKey,
    ): CheckoutLocalizationKey? = error.takeIf { picker.selected == null }

    /** An optional field is only validated when it has content, so a blank optional field is valid. */
    private fun validateRequiredText(
        input: TextInputComponentState,
        error: CheckoutLocalizationKey,
    ): CheckoutLocalizationKey? = error.takeIf { input.requiresValidation() && input.text.isBlank() }
}
