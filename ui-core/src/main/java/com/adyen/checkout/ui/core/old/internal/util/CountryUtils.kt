/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.util.CountryUtils
import com.adyen.checkout.ui.core.old.internal.ui.model.CountryModel
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CountryUtils {

    fun getLocalizedCountries(
        shopperLocale: Locale,
        allowedISOCodes: List<String>? = null,
        comparator: Comparator<CountryModel> = compareBy { it.countryName },
    ): List<CountryModel> {
        return CountryUtils.getCountries(allowedISOCodes)
            .map {
                CountryModel(
                    isoCode = it.isoCode,
                    countryName = CountryUtils.getCountryName(it.isoCode, shopperLocale),
                    callingCode = it.callingCode,
                )
            }
            .sortedWith(comparator)
    }
}
