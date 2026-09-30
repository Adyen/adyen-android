/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.helper.CountryUtils
import com.adyen.checkout.core.components.data.Address
import java.util.Locale

/**
 * An address as the address component collects it, exchanged with the component that hosts the address form.
 *
 * @param country The ISO 3166-1 alpha-2 code of the country.
 * @param postalCode The postal code.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AddressModel(
    val country: String? = null,
    val postalCode: String? = null,
)

/**
 * Maps the address to the payment request model. The API requires every address field once an address is sent, so
 * fields without a value get the placeholders the API accepts.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun AddressModel.toPaymentAddress(): Address = Address(
    city = Address.ADDRESS_NULL_PLACEHOLDER,
    country = country ?: Address.ADDRESS_COUNTRY_NULL_PLACEHOLDER,
    houseNumberOrName = Address.ADDRESS_NULL_PLACEHOLDER,
    postalCode = postalCode ?: Address.ADDRESS_NULL_PLACEHOLDER,
    stateOrProvince = Address.ADDRESS_NULL_PLACEHOLDER,
    street = Address.ADDRESS_NULL_PLACEHOLDER,
)

/**
 * A one-line summary of the address, with the country named in [shopperLocale].
 */
internal fun AddressModel.format(shopperLocale: Locale): String = listOfNotNull(
    postalCode,
    country?.let { CountryUtils.getCountryName(it, shopperLocale) },
).joinToString(separator = ", ")
