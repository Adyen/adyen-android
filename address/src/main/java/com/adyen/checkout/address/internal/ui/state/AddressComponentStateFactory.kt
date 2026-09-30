/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressComponentParams
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class AddressComponentStateFactory(
    private val componentParams: AddressComponentParams,
) : ComponentStateFactory<AddressComponentState> {

    private val countries = CountryUtils.getLocalizedCountries(
        shopperLocale = componentParams.shopperLocale,
        allowedISOCodes = componentParams.supportedCountryCodes.takeIf { it.isNotEmpty() }?.toList(),
    )

    override fun createInitialState() = createState(address = null)

    /**
     * Creates the form for [address]. Without a country to show, the country of the shopper locale is preselected
     * when the shopper can pick it.
     */
    fun createState(address: AddressModel?): AddressComponentState {
        val countryCode = address?.country ?: componentParams.shopperLocale.country
        return AddressComponentState(
            countries = countries,
            country = CountryInputState(selectedCountry = countries.find { it.isoCode == countryCode }),
            postalCode = TextInputComponentState(text = address?.postalCode.orEmpty()),
        )
    }
}
