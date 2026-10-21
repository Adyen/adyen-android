/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class AddressComponentStateFactory(
    private val componentParams: AddressComponentParams,
) : ComponentStateFactory<AddressComponentState> {

    override fun createInitialState() = createState(address = null, countries = emptyList())

    /**
     * Creates the form for [address] with the loaded [countries]. A country is only ever selected from [countries], so
     * a new form, whose countries are not loaded yet, has none. No state is selected either: a state is only ever
     * selected from its loaded states.
     */
    fun createState(address: AddressModel?, countries: List<AddressRegion>): AddressComponentState {
        val countryCode = getDefaultCountryCode(address, countries)
        val spec = AddressSpec.fromCountryCode(countryCode)
        return AddressComponentState(
            countries = countries,
            country = PickerInputComponentState(selected = countryCode),
            street = spec.createTextInput(AddressFormElementId.STREET, address?.street),
            houseNumberOrName = spec.createTextInput(
                AddressFormElementId.HOUSE_NUMBER_OR_NAME,
                address?.houseNumberOrName,
            ),
            postalCode = spec.createTextInput(AddressFormElementId.POSTAL_CODE, address?.postalCode),
            city = spec.createTextInput(AddressFormElementId.CITY, address?.city),
            states = emptyList(),
            stateOrProvince = PickerInputComponentState(),
        )
    }

    /**
     * The country of [address] or, without an address, the country of the shopper locale, when [countries] contains it.
     * [countries] only holds the countries the shopper can pick.
     */
    fun getDefaultCountryCode(address: AddressModel?, countries: List<AddressRegion>): String? {
        // TODO - Address: The default country is internal only. Align it with the other platforms if the team asks for
        //  a merchant-facing default country.
        val countryCode = address?.country ?: componentParams.shopperLocale.country
        return countryCode.takeIf { countries.any { it.code == countryCode } }
    }
}

/** Creates the text input of [id] with the requirement policy this spec gives it. */
private fun AddressSpec.createTextInput(id: AddressFormElementId, text: String?) = TextInputComponentState(
    text = text.orEmpty(),
    requirementPolicy = getFieldSpec(id).requirementPolicy,
)
