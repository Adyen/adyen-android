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
    var apartmentSuite: String = "",
    var city: String = "",
    var country: String = "",
) {
    /**
     * Reset the data.
     *
     * Note: This method is called when country is changed and that's the reason [country] field
     * does not get reset.
     */
    fun reset() {
        postalCode = ""
        street = ""
        stateOrProvince = ""
        houseNumberOrName = ""
        apartmentSuite = ""
        city = ""
    }
}
