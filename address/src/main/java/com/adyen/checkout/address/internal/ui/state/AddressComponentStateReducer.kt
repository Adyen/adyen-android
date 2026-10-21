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
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.address.internal.ui.model.toAddressRegions
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class AddressComponentStateReducer(
    private val componentParams: AddressComponentParams,
    private val componentStateFactory: AddressComponentStateFactory,
) : ComponentStateReducer<AddressComponentState, AddressIntent> {

    override fun reduce(state: AddressComponentState, intent: AddressIntent): AddressComponentState {
        return when (intent) {
            is AddressIntent.UpdateCountry -> state.pickCountry(intent.countryCode)

            is AddressIntent.UpdateStateOrProvince -> state.pickState(intent.stateCode)

            is AddressIntent.UpdateStreet -> state.copy(street = state.street.updateText(intent.street))

            is AddressIntent.UpdateHouseNumberOrName -> state.copy(
                houseNumberOrName = state.houseNumberOrName.updateText(intent.houseNumberOrName),
            )

            is AddressIntent.UpdatePostalCode -> state.copy(postalCode = state.postalCode.updateText(intent.postalCode))

            is AddressIntent.UpdateCity -> state.copy(city = state.city.updateText(intent.city))

            is AddressIntent.Reset -> state.reset(intent.address)

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
        val newSpec = AddressSpec.fromCountryCode(countryCode)
        return copy(
            country = country.copy(selected = countryCode),
            street = street.withRequirementPolicyOf(newSpec, AddressFormElementId.STREET),
            houseNumberOrName = houseNumberOrName.withRequirementPolicyOf(
                newSpec,
                AddressFormElementId.HOUSE_NUMBER_OR_NAME,
            ),
            postalCode = postalCode.withRequirementPolicyOf(newSpec, AddressFormElementId.POSTAL_CODE),
            city = city.withRequirementPolicyOf(newSpec, AddressFormElementId.CITY),
            states = emptyList(),
            stateOrProvince = PickerInputComponentState(),
        )
    }

    /**
     * Replaces the whole state with the form for [address]. The loaded countries do not depend on the address, so they
     * survive a reset. So do the loaded states while the country stays the same, and the state of [address] is selected
     * from them.
     */
    private fun AddressComponentState.reset(address: AddressModel?): AddressComponentState {
        val resetState = componentStateFactory.createState(address, countries)
        if (resetState.country.selected != country.selected) return resetState
        return resetState.copy(
            states = states,
            stateOrProvince = PickerInputComponentState(selected = states.findCode(address?.stateOrProvince)),
        )
    }

    /** Only a loaded country can be picked; any other code, and the selected one, is ignored. */
    private fun AddressComponentState.pickCountry(countryCode: String): AddressComponentState {
        val loadedCode = countries.findCode(countryCode)
        return if (loadedCode == null || loadedCode == country.selected) this else selectCountry(loadedCode)
    }

    /** Only a loaded state can be picked; any other code is ignored. */
    private fun AddressComponentState.pickState(stateCode: String): AddressComponentState {
        val loadedCode = states.findCode(stateCode) ?: return this
        return copy(stateOrProvince = stateOrProvince.copy(selected = loadedCode))
    }

    private fun TextInputComponentState.withRequirementPolicyOf(spec: AddressSpec, id: AddressFormElementId) =
        copy(requirementPolicy = spec.getFieldSpec(id).requirementPolicy)

    /**
     * A country is only ever selected from the loaded countries: a selected one is kept when the list contains it,
     * otherwise the default one is selected when the list contains it. Without countries, after a failure, no country
     * can be selected.
     */
    private fun AddressComponentState.onCountriesLoaded(result: Result<List<AddressItem>>): AddressComponentState {
        val supportedCountryCodes = componentParams.supportedCountryCodes
        val countries = result.getOrNull().orEmpty().toAddressRegions().filter { region ->
            supportedCountryCodes.isEmpty() || region.code in supportedCountryCodes
        }
        val loadedState = copy(countries = countries)
        val countryCode = countries.findCode(country.selected)
            ?: componentStateFactory.getDefaultCountryCode(address = null, countries)
        return if (countryCode == country.selected) loadedState else loadedState.selectCountry(countryCode)
    }

    /**
     * A selected state is only kept when the loaded states contain it, so a failure clears it too. States of a country
     * that is no longer selected are dropped.
     */
    private fun AddressComponentState.onStatesLoaded(
        countryCode: String,
        result: Result<List<AddressItem>>,
    ): AddressComponentState {
        if (countryCode != country.selected) return this

        // TODO - Address: Offer the shopper a way to retry when the states fail to load, instead of having to reopen
        //  the form. Align the design with iOS and Web first. Tracked in COSDK-1635.
        val states = result.getOrNull().orEmpty().toAddressRegions()
        return copy(
            states = states,
            stateOrProvince = stateOrProvince.copy(selected = states.findCode(stateOrProvince.selected)),
        )
    }

    /** The [code] when this list contains it, otherwise null. */
    private fun List<AddressRegion>.findCode(code: String?): String? = code?.takeIf { any { it.code == code } }
}
