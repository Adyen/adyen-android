/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/3/2022.
 */

package com.adyen.checkout.card

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState

data class AddressOutputData(
    val postalCode: FieldState<String>,
    val street: FieldState<String>,
    val stateOrProvince: FieldState<String>,
    val houseNumberOrName: FieldState<String>,
    val apartmentSuite: FieldState<String>,
    val city: FieldState<String>,
    val country: FieldState<String>
) : OutputData {
    override val isValid: Boolean
        get() = postalCode.validation.isValid() &&
            street.validation.isValid() &&
            stateOrProvince.validation.isValid() &&
            houseNumberOrName.validation.isValid() &&
            apartmentSuite.validation.isValid() &&
            city.validation.isValid() &&
            country.validation.isValid()
}
