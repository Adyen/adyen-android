/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class AddressViewStateProducer : ViewStateProducer<AddressComponentState, AddressViewState> {

    override fun produce(state: AddressComponentState) = AddressViewState(
        elements = state.form.elements.map { state.toElement(it.id) },
        countryPickerViewState = CountryPickerViewState(
            countries = state.countries,
            selectedCountry = state.country.selectedCountry,
        ),
    )

    private fun AddressComponentState.toElement(id: AddressFormElementId): AddressFormElement = when (id) {
        AddressFormElementId.COUNTRY -> AddressFormElement.Country(
            selectedCountry = country.selectedCountry,
            errorMessage = country.error?.takeIf { it.isVisible }?.message,
        )

        AddressFormElementId.POSTAL_CODE -> AddressFormElement.PostalCode(
            textInputViewState = postalCode.toViewState(form, focusRequest, id),
        )
    }
}
