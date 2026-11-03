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
 * [shopperLocale]. The street and the house number are one part, joined with a space in the country's order
 * ("10 Downing Street", "Simon Carmiggeltstraat 6"). The state shows as its code.
 */
internal fun AddressModel.format(shopperLocale: Locale): String {
    val order = AddressSpec.fromCountryCode(country).order
    val streetLine = order
        .filter { it == AddressFormElementId.STREET || it == AddressFormElementId.HOUSE_NUMBER_OR_NAME }
        .mapNotNull { if (it == AddressFormElementId.STREET) street else houseNumberOrName }
        .joinToString(" ")
        .ifEmpty { null }
    // Both are next to each other in every spec, so the street line takes the street's place.
    val fields = order.mapNotNull { id ->
        when (id) {
            AddressFormElementId.COUNTRY -> null
            AddressFormElementId.STREET -> streetLine
            AddressFormElementId.HOUSE_NUMBER_OR_NAME -> null
            AddressFormElementId.POSTAL_CODE -> postalCode
            AddressFormElementId.CITY -> city
            AddressFormElementId.STATE_OR_PROVINCE -> stateOrProvince
        }
    }
    val countryName = country?.let { CountryUtils.getCountryName(it, shopperLocale) }
    return (fields + listOfNotNull(countryName)).joinToString(", ")
}
