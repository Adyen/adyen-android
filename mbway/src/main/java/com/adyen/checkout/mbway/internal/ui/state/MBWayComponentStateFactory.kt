/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import java.util.Locale

internal class MBWayComponentStateFactory(
    private val shopperLocale: Locale,
) : ComponentStateFactory<MBWayComponentState> {

    override fun createInitialState() = MBWayComponentState(
        countries = getSupportedCountries(shopperLocale),
        selectedCountryCode = getInitiallySelectedCountry(shopperLocale),
        phoneNumber = TextInputComponentState(),
        isLoading = false,
        // Opens the keyboard on the phone number as soon as the screen appears, skipping the country picker.
        focusRequest = FocusRequest(id = MBWayFieldId.PHONE_NUMBER),
    )

    private fun getSupportedCountries(shopperLocale: Locale): List<CountryModel> =
        CountryUtils.getLocalizedCountries(shopperLocale, SUPPORTED_COUNTRIES)

    private fun getInitiallySelectedCountry(shopperLocale: Locale): CountryModel {
        val countries = getSupportedCountries(shopperLocale)
        return countries.first { it.isoCode == ISO_CODE_PORTUGAL }
    }

    companion object {
        private const val ISO_CODE_PORTUGAL = "PT"
        private const val ISO_CODE_SPAIN = "ES"
        private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)
    }
}
