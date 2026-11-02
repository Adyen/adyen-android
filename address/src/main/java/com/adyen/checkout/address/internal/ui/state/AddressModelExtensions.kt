/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/11/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.common.internal.helper.CountryUtils
import java.util.Locale

/**
 * The address on one line, as the host's row shows it: the fields in the country's order, then the country's name in
 * [shopperLocale]. The state shows as its code.
 */
internal fun AddressModel.format(shopperLocale: Locale): String {
    val fields = AddressSpec.fromCountryCode(country).order.mapNotNull { id ->
        when (id) {
            AddressFormElementId.COUNTRY -> null
            AddressFormElementId.STREET -> street
            AddressFormElementId.HOUSE_NUMBER_OR_NAME -> houseNumberOrName
            AddressFormElementId.POSTAL_CODE -> postalCode
            AddressFormElementId.CITY -> city
            AddressFormElementId.STATE_OR_PROVINCE -> stateOrProvince
        }
    }
    val countryName = country?.let { CountryUtils.getCountryName(it, shopperLocale) }
    return (fields + listOfNotNull(countryName)).joinToString(", ")
}
