/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressField
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.address.internal.ui.model.toAddressRegions
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class AddressComponentStateReducer(
    private val componentParams: AddressComponentParams,
    private val componentStateFactory: AddressComponentStateFactory,
) : ComponentStateReducer<AddressComponentState, AddressIntent> {

    override fun reduce(state: AddressComponentState, intent: AddressIntent): AddressComponentState {
        return when (intent) {
            is AddressIntent.UpdateCountry ->
                if (intent.countryCode == state.country.selected) state else state.selectCountry(intent.countryCode)

            is AddressIntent.UpdateStateOrProvince -> state.copy(
                stateOrProvince = state.stateOrProvince.updateValue(intent.value),
            )

            is AddressIntent.UpdateStreet -> state.copy(street = state.street.updateText(intent.street))

            is AddressIntent.UpdateHouseNumberOrName -> state.copy(
                houseNumberOrName = state.houseNumberOrName.updateText(intent.houseNumberOrName),
            )

            is AddressIntent.UpdatePostalCode -> state.copy(postalCode = state.postalCode.updateText(intent.postalCode))

            is AddressIntent.UpdateCity -> state.copy(city = state.city.updateText(intent.city))

            // The loaded countries do not depend on the address, so they survive a prefill.
            is AddressIntent.Prefill -> componentStateFactory.createState(intent.address).copy(
                countries = state.countries,
                isLoadingCountries = state.isLoadingCountries,
            )

            is AddressIntent.CountriesLoaded -> state.onCountriesLoaded(intent.result)

            is AddressIntent.StatesLoaded -> state.onStatesLoaded(intent.countryCode, intent.result)

            is AddressIntent.UpdateFieldFocus,
            is AddressIntent.FocusRequestConsumed,
            is AddressIntent.HighlightValidationErrors -> state
        }
    }

    /**
     * Selects [countryCode] and switches to its layout. What the shopper typed is kept, as on the other platforms; only
     * the state or province is reset, because the states belong to the previous country. The text fields take their
     * requirement policy from the new spec.
     */
    private fun AddressComponentState.selectCountry(countryCode: String?): AddressComponentState {
        val spec = AddressSpec.fromCountryCode(countryCode)
        return copy(
            spec = spec,
            country = country.copy(selected = countryCode),
            street = street.withRequirementPolicyOf(spec, AddressField.STREET),
            houseNumberOrName = houseNumberOrName.withRequirementPolicyOf(spec, AddressField.HOUSE_NUMBER_OR_NAME),
            postalCode = postalCode.withRequirementPolicyOf(spec, AddressField.POSTAL_CODE),
            city = city.withRequirementPolicyOf(spec, AddressField.CITY),
            stateOrProvince = createStateOrProvince(countryCode, pendingCode = null),
        )
    }

    private fun TextInputComponentState.withRequirementPolicyOf(spec: AddressSpec, field: AddressField) =
        copy(requirementPolicy = spec.getFieldSpec(field).requirementPolicy)

    /**
     * Keeps the selected country when the countries fail to load, so a prefilled or default country still works
     * offline. Clears it when the loaded list does not contain it.
     */
    private fun AddressComponentState.onCountriesLoaded(result: Result<List<AddressItem>>): AddressComponentState {
        val supportedCountryCodes = componentParams.supportedCountryCodes
        val countries = result.getOrNull().orEmpty().toAddressRegions().filter { region ->
            supportedCountryCodes.isEmpty() || region.code in supportedCountryCodes
        }
        val loadedState = copy(countries = countries, isLoadingCountries = false)
        val selected = country.selected
        val isSelectionUnlisted = selected != null && countries.isNotEmpty() && countries.none { it.code == selected }
        return if (isSelectionUnlisted) loadedState.selectCountry(null) else loadedState
    }

    private fun AddressComponentState.onStatesLoaded(
        countryCode: String,
        result: Result<List<AddressItem>>,
    ): AddressComponentState {
        val loading = stateOrProvince as? StateOrProvinceState.Loading
        if (countryCode != country.selected || loading == null) return this

        val pendingCode = loading.pendingCode
        val stateOrProvince = result.fold(
            onSuccess = { items ->
                val regions = items.toAddressRegions()
                if (regions.isEmpty()) {
                    StateOrProvinceState.Unavailable
                } else {
                    val selected = pendingCode?.takeIf { code -> regions.any { it.code == code } }
                    StateOrProvinceState.Options(
                        regions = regions,
                        picker = PickerInputComponentState(selected = selected),
                    )
                }
            },
            onFailure = {
                StateOrProvinceState.FreeText(input = TextInputComponentState(text = pendingCode.orEmpty()))
            },
        )
        return copy(stateOrProvince = stateOrProvince)
    }

    private fun StateOrProvinceState.updateValue(value: String): StateOrProvinceState = when (this) {
        is StateOrProvinceState.Options -> copy(picker = picker.copy(selected = value))
        is StateOrProvinceState.FreeText -> copy(input = input.updateText(value))
        is StateOrProvinceState.Loading,
        StateOrProvinceState.Unavailable -> this
    }
}
