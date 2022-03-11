/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/3/2022.
 */

package com.adyen.checkout.card

data class AddressInputModel(
    var postalCode: String = "",
    var street: String = "",
    var stateOrProvince: String = "",
    var houseNumberOrName: String = "",
    var city: String = "",
    var country: String = "",
)