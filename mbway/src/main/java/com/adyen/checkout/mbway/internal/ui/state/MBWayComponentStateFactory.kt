/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class MBWayComponentStateFactory(
    private val componentParams: ComponentParams,
) : ComponentStateFactory<MBWayComponentState> {

    override fun createInitialState() = MBWayComponentState(
        countries = getSupportedCountries(componentParams),
        selectedCountryCode = getInitiallySelectedCountry(componentParams),
        phoneNumber = TextInputComponentState(isFocused = true),
        isLoading = false,
    )

    private fun getSupportedCountries(componentParams: ComponentParams): List<CountryModel> =
        CountryUtils.getLocalizedCountries(componentParams.shopperLocale, SUPPORTED_COUNTRIES)

    private fun getInitiallySelectedCountry(componentParams: ComponentParams): CountryModel {
        val countries = getSupportedCountries(componentParams)
        return countries.firstOrNull { it.isoCode == ISO_CODE_PORTUGAL }
            ?: countries.firstOrNull()
            ?: throw IllegalArgumentException("Countries list can not be null")
    }

    companion object {
        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"

        val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
