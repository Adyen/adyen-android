/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.model

import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.IntegrationRegion
import com.adyen.checkout.example.provider.LocaleProvider
import com.adyen.checkout.example.ui.compose.UIText
import java.util.Locale
import javax.inject.Inject

internal class IntegrationRegionUIMapper @Inject constructor(
    private val localeProvider: LocaleProvider,
) {
    fun getIntegrationRegionDisplayData(integrationRegion: IntegrationRegion): IntegrationRegionDisplayData {
        val integrationLocale = Locale("", integrationRegion.countryCode)
        val localizedCountryName = integrationLocale.getDisplayCountry(localeProvider.locale)
        val uiText = UIText.Resource(
            R.string.settings_integration_region_display_value,
            localizedCountryName,
            integrationRegion.countryCode,
            integrationRegion.currency,
        )
        return IntegrationRegionDisplayData(integrationRegion, localizedCountryName, uiText)
    }
}

data class IntegrationRegionDisplayData(
    val integrationRegion: IntegrationRegion,
    val localizedCountryName: String,
    val uiText: UIText,
)
