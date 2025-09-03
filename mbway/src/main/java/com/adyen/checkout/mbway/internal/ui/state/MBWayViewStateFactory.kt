/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ViewStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState

internal class MBWayViewStateFactory(
    private val componentParams: ComponentParams,
) : ViewStateFactory<MBWayViewState> {

    override fun createDefaultViewState() = MBWayViewState(
        countries = getSupportedCountries(componentParams),
        countryCode = getInitiallySelectedCountry(componentParams),
        phoneNumber = TextInputState(isFocused = true),
        test = TextInputState(),
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
