/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressField
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.address.internal.ui.model.AddressSpec
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class AddressComponentStateFactory(
    private val componentParams: AddressComponentParams,
) : ComponentStateFactory<AddressComponentState> {

    override fun createInitialState() = createState(address = null)

    /**
     * Creates the form for [address]. Without an address, the country of the shopper locale is preselected when the
     * shopper can pick it. The countries are not loaded yet, so the selection is kept as a code.
     */
    fun createState(address: AddressModel?): AddressComponentState {
        val countryCode = address?.country ?: getDefaultCountryCode()
        val spec = AddressSpec.fromCountryCode(countryCode)
        return AddressComponentState(
            spec = spec,
            countries = emptyList(),
            isLoadingCountries = true,
            country = PickerInputComponentState(selected = countryCode),
            street = spec.createTextInput(AddressField.STREET, address?.street),
            houseNumberOrName = spec.createTextInput(AddressField.HOUSE_NUMBER_OR_NAME, address?.houseNumberOrName),
            postalCode = spec.createTextInput(AddressField.POSTAL_CODE, address?.postalCode),
            city = spec.createTextInput(AddressField.CITY, address?.city),
            stateOrProvince = createStateOrProvince(countryCode, pendingCode = address?.stateOrProvince),
        )
    }

    // TODO - Address: The default country is internal only. Align it with the other platforms if the team asks for a
    //  merchant-facing default country.
    private fun getDefaultCountryCode(): String? {
        val supportedCountryCodes = componentParams.supportedCountryCodes
        return componentParams.shopperLocale.country.takeIf { countryCode ->
            countryCode.isNotEmpty() && (supportedCountryCodes.isEmpty() || countryCode in supportedCountryCodes)
        }
    }
}

/**
 * The states of a selected country are loading until they arrive, keeping [pendingCode] to select it then. Without a
 * country there are no states to show.
 */
internal fun createStateOrProvince(countryCode: String?, pendingCode: String?): StateOrProvinceState =
    if (countryCode != null) {
        StateOrProvinceState.Loading(pendingCode = pendingCode)
    } else {
        StateOrProvinceState.Unavailable
    }

/** Creates the text input of [field] with the requirement policy this spec gives it. */
private fun AddressSpec.createTextInput(field: AddressField, text: String?) = TextInputComponentState(
    text = text.orEmpty(),
    requirementPolicy = getFieldSpec(field).requirementPolicy,
)
