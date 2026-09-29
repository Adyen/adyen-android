/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator

internal class AddressComponentStateValidator : ComponentStateValidator<AddressComponentState> {

    override fun validate(state: AddressComponentState): AddressComponentState {
        val countryError = if (state.country.selectedCountry == null) {
            CheckoutLocalizationKey.ADDRESS_COUNTRY_ERROR
        } else {
            null
        }
        return state.copy(
            country = state.country.updateError(countryError),
            postalCode = state.postalCode.updateError(validatePostalCode(state.postalCode.text)),
        )
    }

    private fun validatePostalCode(postalCode: String): CheckoutLocalizationKey? {
        val isValid = postalCode.isNotBlank() && postalCode.length <= PostalCodeProperties.POSTAL_CODE_MAX_LENGTH
        return if (isValid) null else CheckoutLocalizationKey.ADDRESS_POSTAL_CODE_ERROR
    }

    override fun isValid(state: AddressComponentState): Boolean = state.form.isFormValid
}
