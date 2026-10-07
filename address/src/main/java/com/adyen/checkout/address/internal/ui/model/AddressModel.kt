/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.model

/**
 * An address as the address component collects it. Field names follow the API.
 *
 * @param country The ISO 3166-1 alpha-2 code of the country.
 * @param stateOrProvince The ISO 3166-2 code of the state when it was picked, or the shopper's text when the states
 * could not be loaded.
 */
internal data class AddressModel(
    val country: String?,
    val street: String?,
    val houseNumberOrName: String?,
    val postalCode: String?,
    val city: String?,
    val stateOrProvince: String?,
)
