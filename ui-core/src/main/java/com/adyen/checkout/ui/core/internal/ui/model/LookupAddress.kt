/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/12/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.model

data class LookupAddress(
    val id: String,
    val address: AddressInputModel,
    val isLoading: Boolean = false
) {
    override fun toString(): String {
        return listOf(
            address.street,
            address.houseNumberOrName,
            address.apartmentSuite,
            address.postalCode,
            address.city,
            address.stateOrProvince,
            address.country
        ).filter { it.isNotBlank() }.joinToString(" ")
    }

    val title
        get() = address.street.ifBlank {
            toString()
        }

    val subtitle
        get() = if (address.street.isBlank()) {
            ""
        } else {
            toString()
        }
}
