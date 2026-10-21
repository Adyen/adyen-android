/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.properties.PostalCodeProperties
import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState
import java.util.Locale

/**
 * Resolves what the component state keeps as codes: the names of the selected country and state, the labels of the
 * selected country's layout and the postal-code maximum. Composables look nothing up.
 *
 * @param shopperLocale Names the selected country while the countries are not loaded.
 */
internal class AddressViewStateProducer(
    private val shopperLocale: Locale,
) : ViewStateProducer<AddressComponentState, AddressViewState> {

    override fun produce(state: AddressComponentState): AddressViewState {
        val selectedCountry = state.getSelectedCountry()
        val selectedState = state.getSelectedState()
        return AddressViewState(
            elements = state.form.elements.map { state.toElement(it.id, selectedCountry, selectedState) },
            countryPickerViewState = PickerViewState(
                title = state.getLabel(AddressFormElementId.COUNTRY),
                regions = state.countries,
                selected = selectedCountry,
            ),
            statePickerViewState = if (state.spec.hasStates) {
                PickerViewState(
                    title = state.getLabel(AddressFormElementId.STATE_OR_PROVINCE),
                    regions = state.states,
                    selected = selectedState,
                )
            } else {
                null
            },
        )
    }

    private fun AddressComponentState.toElement(
        id: AddressFormElementId,
        selectedCountry: AddressRegion?,
        selectedState: AddressRegion?,
    ): AddressFormElement = when (id) {
        AddressFormElementId.COUNTRY -> AddressFormElement.Country(
            label = getLabel(id),
            selected = selectedCountry,
            errorMessage = country.getVisibleErrorMessage(),
        )

        AddressFormElementId.STREET -> AddressFormElement.Street(
            label = getLabel(id),
            textInputViewState = street.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.HOUSE_NUMBER_OR_NAME -> AddressFormElement.HouseNumberOrName(
            label = getLabel(id),
            textInputViewState = houseNumberOrName.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.POSTAL_CODE -> AddressFormElement.PostalCode(
            label = getLabel(id),
            maxLength = PostalCodeProperties.getMaxLength(country.selected),
            textInputViewState = postalCode.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.CITY -> AddressFormElement.City(
            label = getLabel(id),
            textInputViewState = city.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.STATE_OR_PROVINCE -> AddressFormElement.StateOrProvince(
            label = getLabel(id),
            selected = selectedState,
            errorMessage = stateOrProvince.getVisibleErrorMessage(),
        )
    }

    private fun AddressComponentState.getLabel(id: AddressFormElementId) = spec.getFieldSpec(id).label

    /** The loaded name of the selected country or, while the countries are not loaded, the platform's name for it. */
    private fun AddressComponentState.getSelectedCountry(): AddressRegion? = country.selected?.let { code ->
        countries.find { it.code == code } ?: AddressRegion(code, CountryUtils.getCountryName(code, shopperLocale))
    }

    /** The selected state, which is always one of the loaded states. */
    private fun AddressComponentState.getSelectedState(): AddressRegion? =
        stateOrProvince.selected?.let { code -> states.find { it.code == code } }

    private fun PickerInputComponentState<*>.getVisibleErrorMessage(): CheckoutLocalizationKey? =
        error?.takeIf { it.isVisible }?.message
}
