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
) {
    constructor(addressOutputData: AddressOutputData) : this(
        postalCode = addressOutputData.postalCode.value,
        street = addressOutputData.street.value,
        stateOrProvince = addressOutputData.stateOrProvince.value,
        houseNumberOrName = addressOutputData.houseNumberOrName.value,
        city = addressOutputData.city.value,
        country = addressOutputData.country.value,
    )

    fun reset() {
        postalCode = ""
        street = ""
        stateOrProvince = ""
        houseNumberOrName = ""
        city = ""
        country = ""
    }
}
