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
            is AddressIntent.UpdateCountry ->
                if (intent.countryCode == state.country.selected) state else state.selectCountry(intent.countryCode)

            is AddressIntent.UpdateStateOrProvince -> state.selectState(intent.stateCode)

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
     * survive a reset. So do the loaded states while the country stays the same; the state of [address] is cleared when
     * they do not contain it.
     */
    private fun AddressComponentState.reset(address: AddressModel?): AddressComponentState {
        val resetState = componentStateFactory.createState(address).copy(countries = countries)
        if (resetState.country.selected != country.selected) return resetState
        return resetState.copy(states = states).clearUnlistedState()
    }

    /** Only a loaded state can be selected; any other code is ignored. */
    private fun AddressComponentState.selectState(stateCode: String): AddressComponentState {
        val loadedCode = states.findCode(stateCode) ?: return this
        return copy(stateOrProvince = stateOrProvince.copy(selected = loadedCode))
    }

    private fun TextInputComponentState.withRequirementPolicyOf(spec: AddressSpec, id: AddressFormElementId) =
        copy(requirementPolicy = spec.getFieldSpec(id).requirementPolicy)

    /**
     * Keeps the selected country when the countries fail to load, so a prefilled or default country still works
     * offline. Clears it when the loaded list does not contain it.
     */
    private fun AddressComponentState.onCountriesLoaded(result: Result<List<AddressItem>>): AddressComponentState {
        val supportedCountryCodes = componentParams.supportedCountryCodes
        val countries = result.getOrNull().orEmpty().toAddressRegions().filter { region ->
            supportedCountryCodes.isEmpty() || region.code in supportedCountryCodes
        }
        val loadedState = copy(countries = countries)
        return if (countries.isMissing(country.selected)) loadedState.selectCountry(null) else loadedState
    }

    /**
     * Keeps the selected state when the states fail to load, so a saved state still works offline, as the country does.
     * Clears it when the loaded states do not contain it. States of a country that is no longer selected are dropped.
     */
    private fun AddressComponentState.onStatesLoaded(
        countryCode: String,
        result: Result<List<AddressItem>>,
    ): AddressComponentState {
        if (countryCode != country.selected) return this

        // TODO - Address: Offer the shopper a way to retry when the states fail to load, instead of having to reopen
        //  the form. Align the design with iOS and Web first. Tracked in COSDK-1635.
        return copy(states = result.getOrNull().orEmpty().toAddressRegions()).clearUnlistedState()
    }

    /** Clears the selected state when the loaded states leave it out, as [onCountriesLoaded] does for the country. */
    private fun AddressComponentState.clearUnlistedState(): AddressComponentState {
        if (!states.isMissing(stateOrProvince.selected)) return this
        return copy(stateOrProvince = stateOrProvince.copy(selected = null))
    }

    /** The [code] when this list contains it, otherwise null. */
    private fun List<AddressRegion>.findCode(code: String?): String? = code?.takeIf { any { it.code == code } }

    /** Whether this loaded list leaves out the selected [code]. An empty list leaves nothing out. */
    private fun List<AddressRegion>.isMissing(code: String?): Boolean =
        code != null && isNotEmpty() && none { it.code == code }
}
