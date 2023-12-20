/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/12/2023.
 */

package com.adyen.checkout.components.core

data class AddressInputModel(
    var postalCode: String = "",
    var street: String = "",
    var stateOrProvince: String = "",
    var houseNumberOrName: String = "",
    var apartmentSuite: String = "",
    var city: String = "",
    var country: String = "",
) {

    fun set(addressInputModel: AddressInputModel) {
        postalCode = addressInputModel.postalCode
        street = addressInputModel.street
        stateOrProvince = addressInputModel.stateOrProvince
        houseNumberOrName = addressInputModel.houseNumberOrName
        apartmentSuite = addressInputModel.apartmentSuite
        city = addressInputModel.city
        country = addressInputModel.country
    }

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

    /**
     * Reset the data.
     *
     * Note: This method is called from address lookup and all the form needs to be reset.
     */
    fun resetAll() {
        country = ""
        postalCode = ""
        street = ""
        stateOrProvince = ""
        houseNumberOrName = ""
        apartmentSuite = ""
        city = ""
    }

    val isEmpty
        get() = postalCode.isEmpty() &&
            street.isEmpty() &&
            stateOrProvince.isEmpty() &&
            houseNumberOrName.isEmpty() &&
            apartmentSuite.isEmpty() &&
            city.isEmpty() &&
            country.isEmpty()
}
