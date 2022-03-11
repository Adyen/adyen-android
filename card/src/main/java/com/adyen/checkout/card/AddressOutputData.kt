/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/3/2022.
 */

package com.adyen.checkout.card

data class AddressOutputData(
    val postalCode: String,
    val street: String,
    val stateOrProvince: String,
    val houseNumberOrName: String,
    val city: String,
    val country: String
)