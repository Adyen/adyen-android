/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressField
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
        return AddressViewState(
            elements = state.form.elements.mapNotNull { state.toElement(it.id, selectedCountry) },
            countryPickerViewState = PickerViewState(
                title = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
                regions = state.countries,
                selected = selectedCountry,
            ),
            statePickerViewState = state.stateOrProvince.getOptions()?.let { options ->
                PickerViewState(
                    title = state.getLabel(AddressField.STATE_OR_PROVINCE),
                    regions = options.regions,
                    selected = options.getSelectedRegion(),
                )
            },
        )
    }

    /** Null only for an element that has nothing to render, which the form does not contain either. */
    private fun AddressComponentState.toElement(
        id: AddressFormElementId,
        selectedCountry: AddressRegion?,
    ): AddressFormElement? = when (id) {
        AddressFormElementId.COUNTRY -> AddressFormElement.Country(
            label = CheckoutLocalizationKey.ADDRESS_COUNTRY_LABEL,
            selected = selectedCountry,
            errorMessage = country.getVisibleErrorMessage(),
        )

        AddressFormElementId.STREET -> AddressFormElement.Street(
            label = getLabel(AddressField.STREET),
            textInputViewState = street.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.HOUSE_NUMBER_OR_NAME -> AddressFormElement.HouseNumberOrName(
            label = getLabel(AddressField.HOUSE_NUMBER_OR_NAME),
            textInputViewState = houseNumberOrName.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.POSTAL_CODE -> AddressFormElement.PostalCode(
            label = getLabel(AddressField.POSTAL_CODE),
            maxLength = PostalCodeProperties.getMaxLength(country.selected),
            textInputViewState = postalCode.toViewState(form, focusRequest, id),
        )

        AddressFormElementId.CITY -> AddressFormElement.City(
            label = getLabel(AddressField.CITY),
            textInputViewState = city.toViewState(form, focusRequest, id),
        )

        // TODO - Address: Loading and unavailable states render nothing for now. Once their UI is designed and aligned
        //  with iOS and Web, this becomes an exhaustive when with a row per state. Tracked in COSDK-1635.
        AddressFormElementId.STATE_OR_PROVINCE -> stateOrProvince.getOptions()?.let { options ->
            AddressFormElement.StateOrProvince(
                label = getLabel(AddressField.STATE_OR_PROVINCE),
                selected = options.getSelectedRegion(),
                errorMessage = options.picker.getVisibleErrorMessage(),
            )
        }
    }

    /** The states to pick from, or null while there are none to render. */
    private fun StateOrProvinceState.getOptions(): StateOrProvinceState.Options? = when (this) {
        is StateOrProvinceState.Options -> this
        is StateOrProvinceState.Loading,
        StateOrProvinceState.Unavailable -> null
    }

    private fun AddressComponentState.getLabel(field: AddressField) = spec.getFieldSpec(field).label

    /** The loaded name of the selected country or, while the countries are not loaded, the platform's name for it. */
    private fun AddressComponentState.getSelectedCountry(): AddressRegion? = country.selected?.let { code ->
        countries.find { it.code == code } ?: AddressRegion(code, CountryUtils.getCountryName(code, shopperLocale))
    }

    private fun StateOrProvinceState.Options.getSelectedRegion(): AddressRegion? =
        picker.selected?.let { code -> regions.find { it.code == code } }

    private fun PickerInputComponentState<*>.getVisibleErrorMessage(): CheckoutLocalizationKey? =
        error?.takeIf { it.isVisible }?.message
}
