/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/2/2025.
 */

package com.adyen.checkout.mbway.internal.ui

import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateStateFactory
import com.adyen.checkout.mbway.internal.ui.model.MBWayDelegateState
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.util.CountryUtils

internal class MBWayDelegateStateFactory(
    private val componentParams: ButtonComponentParams
) : DelegateStateFactory<MBWayDelegateState, MBWayFieldId> {

    override fun createDefaultDelegateState() = MBWayDelegateState(
        countries = getSupportedCountries(componentParams),
        countryCodeFieldState = ComponentFieldDelegateState(getInitiallySelectedCountry(componentParams)),
    )

    private fun getSupportedCountries(componentParams: ComponentParams): List<CountryModel> =
        CountryUtils.getLocalizedCountries(componentParams.shopperLocale, SUPPORTED_COUNTRIES)

    private fun getInitiallySelectedCountry(componentParams: ComponentParams): CountryModel {
        val countries = getSupportedCountries(componentParams)
        return countries.firstOrNull { it.isoCode == ISO_CODE_PORTUGAL } ?: countries.firstOrNull()
        ?: throw IllegalArgumentException("Countries list can not be null")
    }

    override fun getFieldIds(): List<MBWayFieldId> = MBWayFieldId.entries

    companion object {
        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"

        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
