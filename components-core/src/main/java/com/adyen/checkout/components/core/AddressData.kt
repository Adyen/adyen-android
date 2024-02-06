/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/2/2024.
 */

package com.adyen.checkout.components.core

import androidx.annotation.RestrictTo

data class AddressData(
    val postalCode: String,
    val street: String,
    val stateOrProvince: String,
    val houseNumberOrName: String,
    val apartmentSuite: String?,
    val city: String,
    val country: String,
)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun AddressData.mapToAddressInputModel() = AddressInputModel(
    postalCode = postalCode,
    street = street,
    stateOrProvince = stateOrProvince,
    houseNumberOrName = houseNumberOrName,
    apartmentSuite = apartmentSuite.orEmpty(),
    city = city,
    country = country,
)
